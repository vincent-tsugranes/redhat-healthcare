#!/bin/bash

# Create Practitioners and Appointments Test Data
# This script generates 30 practitioners and 100 appointments for existing patients

set -e

PRACTITIONER_API="http://localhost:8083/fhir"
APPOINTMENT_API="http://localhost:8084/fhir"
PATIENT_API="http://localhost:8080/fhir"

echo "======================================"
echo "Creating Practitioners and Appointments"
echo "======================================"

# Arrays for practitioner data
declare -a FIRST_NAMES=("James" "Michael" "Robert" "John" "David" "William" "Richard" "Joseph" "Thomas" "Christopher" "Mary" "Patricia" "Jennifer" "Linda" "Elizabeth" "Barbara" "Susan" "Jessica" "Sarah" "Karen" "Nancy" "Lisa" "Margaret" "Betty" "Sandra" "Ashley" "Kimberly" "Emily" "Donna" "Michelle")

declare -a LAST_NAMES=("Smith" "Johnson" "Williams" "Brown" "Jones" "Garcia" "Miller" "Davis" "Rodriguez" "Martinez" "Hernandez" "Lopez" "Gonzalez" "Wilson" "Anderson" "Thomas" "Taylor" "Moore" "Jackson" "Martin" "Lee" "Thompson" "White" "Harris" "Clark" "Lewis" "Robinson" "Walker" "Young" "Allen")

declare -a SPECIALTIES=("Family Medicine" "Internal Medicine" "Pediatrics" "Cardiology" "Dermatology" "Orthopedic Surgery" "Psychiatry" "Obstetrics and Gynecology" "Emergency Medicine" "Radiology" "Anesthesiology" "Pathology" "General Surgery" "Neurology" "Ophthalmology" "Urology" "Oncology" "Gastroenterology" "Pulmonology" "Endocrinology")

declare -a SPECIALTY_CODES=("family" "internal" "pediatrics" "cardiology" "dermatology" "orthopedics" "psychiatry" "obgyn" "emergency" "radiology" "anesthesiology" "pathology" "surgery" "neurology" "ophthalmology" "urology" "oncology" "gastroenterology" "pulmonology" "endocrinology")

declare -a NC_CITIES=("Charlotte" "Raleigh" "Durham" "Greensboro" "WinstonSalem" "Fayetteville" "Cary" "Wilmington" "Asheville" "ChapelHill")

declare -a APPT_TYPES=("CHECKUP" "FOLLOWUP" "ROUTINE" "WALKIN" "EMERGENCY")
declare -a APPT_TYPE_DISPLAYS=("Routine Checkup" "Follow-up Visit" "Routine Care" "Walk-in" "Emergency")

declare -a APPT_STATUSES=("booked" "fulfilled" "cancelled" "arrived" "noshow" "pending")

declare -a REASONS=("Annual Physical Examination" "Follow-up consultation" "New patient visit" "Medication review" "Lab results review" "Symptom evaluation" "Preventive care" "Chronic disease management" "Post-operative follow-up" "Vaccination")

# Function to get random element from array
get_random_first_name() {
  local idx=$((RANDOM % ${#FIRST_NAMES[@]}))
  echo "${FIRST_NAMES[$idx]}"
}

get_random_last_name() {
  local idx=$((RANDOM % ${#LAST_NAMES[@]}))
  echo "${LAST_NAMES[$idx]}"
}

get_random_specialty() {
  local idx=$((RANDOM % ${#SPECIALTIES[@]}))
  echo "${SPECIALTIES[$idx]}"
}

get_random_specialty_code() {
  local idx=$((RANDOM % ${#SPECIALTY_CODES[@]}))
  echo "${SPECIALTY_CODES[$idx]}"
}

get_random_city() {
  local idx=$((RANDOM % ${#NC_CITIES[@]}))
  echo "${NC_CITIES[$idx]}"
}

get_random_appt_type() {
  local idx=$((RANDOM % ${#APPT_TYPES[@]}))
  echo "${APPT_TYPES[$idx]}"
}

get_random_appt_type_display() {
  local idx=$((RANDOM % ${#APPT_TYPE_DISPLAYS[@]}))
  echo "${APPT_TYPE_DISPLAYS[$idx]}"
}

get_random_appt_status() {
  local idx=$((RANDOM % ${#APPT_STATUSES[@]}))
  echo "${APPT_STATUSES[$idx]}"
}

get_random_reason() {
  local idx=$((RANDOM % ${#REASONS[@]}))
  echo "${REASONS[$idx]}"
}

# Function to generate NPI (National Provider Identifier)
generate_npi() {
  echo "1$(printf '%09d' $((RANDOM * RANDOM % 1000000000)))"
}

# Function to generate a random phone number
generate_phone() {
  echo "$(printf '(%03d) %03d-%04d' $((RANDOM % 900 + 100)) $((RANDOM % 900 + 100)) $((RANDOM % 10000)))"
}

# Function to generate street address
generate_street() {
  local num=$((RANDOM % 9000 + 1000))
  declare -a streets=("Medical Center Dr" "Healthcare Blvd" "Wellness Way" "Hospital Rd" "Clinic Ave" "Doctor St" "Health Plaza" "Care Circle")
  local idx=$((RANDOM % ${#streets[@]}))
  echo "$num ${streets[$idx]}"
}

# Function to get state for city
get_state() {
  echo "NC"
}

# Function to generate zip code
generate_zip() {
  echo "$(printf '27%03d' $((RANDOM % 1000)))"
}

# Function to generate random date in the past (for past appointments)
generate_past_date() {
  local days_ago=$((RANDOM % 180 + 1))  # 1-180 days ago
  date -v-${days_ago}d -u +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -d "$days_ago days ago" -u +"%Y-%m-%dT%H:%M:%SZ"
}

# Function to generate random date in the future (for upcoming appointments)
generate_future_date() {
  local days_ahead=$((RANDOM % 90 + 1))  # 1-90 days ahead
  date -v+${days_ahead}d -u +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -d "$days_ahead days" -u +"%Y-%m-%dT%H:%M:%SZ"
}

# Function to add hours to a date
add_hours() {
  local base_date="$1"
  local hours="$2"
  # Convert to timestamp, add hours, convert back
  local timestamp
  if date -j -f "%Y-%m-%dT%H:%M:%SZ" "$base_date" "+%s" >/dev/null 2>&1; then
    # macOS
    timestamp=$(date -j -f "%Y-%m-%dT%H:%M:%SZ" "$base_date" "+%s")
    timestamp=$((timestamp + hours * 3600))
    date -j -f "%s" "$timestamp" -u "+%Y-%m-%dT%H:%M:%SZ"
  else
    # Linux
    timestamp=$(date -d "$base_date" "+%s")
    timestamp=$((timestamp + hours * 3600))
    date -d "@$timestamp" -u "+%Y-%m-%dT%H:%M:%SZ"
  fi
}

echo ""
echo "Step 1: Creating 30 Practitioners..."
echo "======================================"

PRACTITIONER_IDS=()

for i in $(seq 1 30); do
  FIRST_NAME=$(get_random_first_name)
  LAST_NAME=$(get_random_last_name)
  NPI=$(generate_npi)
  SPECIALTY=$(get_random_specialty)
  SPECIALTY_CODE=$(get_random_specialty_code)
  PHONE=$(generate_phone)
  EMAIL="dr.$(echo $FIRST_NAME | tr '[:upper:]' '[:lower:]').$(echo $LAST_NAME | tr '[:upper:]' '[:lower:]')@healthcare.example.com"
  CITY=$(get_random_city)
  STREET=$(generate_street)
  STATE=$(get_state)
  ZIP=$(generate_zip)

  # Randomly assign gender
  if [ $((RANDOM % 2)) -eq 0 ]; then
    GENDER="male"
  else
    GENDER="female"
  fi

  PRACTITIONER_JSON=$(cat <<EOF
{
  "resourceType": "Practitioner",
  "identifier": [
    {
      "system": "http://hl7.org/fhir/sid/us-npi",
      "value": "$NPI"
    }
  ],
  "active": true,
  "name": [
    {
      "use": "official",
      "family": "$LAST_NAME",
      "given": ["$FIRST_NAME"],
      "prefix": ["Dr."]
    }
  ],
  "telecom": [
    {
      "system": "phone",
      "value": "$PHONE",
      "use": "work"
    },
    {
      "system": "email",
      "value": "$EMAIL",
      "use": "work"
    }
  ],
  "address": [
    {
      "use": "work",
      "type": "physical",
      "line": ["$STREET"],
      "city": "$CITY",
      "state": "$STATE",
      "postalCode": "$ZIP",
      "country": "US"
    }
  ],
  "gender": "$GENDER",
  "qualification": [
    {
      "code": {
        "coding": [
          {
            "system": "http://terminology.hl7.org/CodeSystem/v2-0360",
            "code": "$SPECIALTY_CODE",
            "display": "$SPECIALTY"
          }
        ],
        "text": "$SPECIALTY"
      }
    }
  ]
}
EOF
)

  RESPONSE=$(curl -s -X POST "$PRACTITIONER_API/Practitioner" \
    -H "Content-Type: application/json" \
    -d "$PRACTITIONER_JSON")

  PRAC_ID=$(echo "$RESPONSE" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data.get('id', ''))")

  if [ -n "$PRAC_ID" ]; then
    PRACTITIONER_IDS+=("$PRAC_ID")
    echo "✓ Created Practitioner $i: Dr. $FIRST_NAME $LAST_NAME ($SPECIALTY) - ID: $PRAC_ID"
  else
    echo "✗ Failed to create Practitioner $i"
  fi
done

echo ""
echo "Created ${#PRACTITIONER_IDS[@]} practitioners"

# Get list of existing patients
echo ""
echo "Step 2: Fetching existing patients..."
echo "======================================"

PATIENT_BUNDLE=$(curl -s "$PATIENT_API/Patient?_count=100")
PATIENT_IDS=($(echo "$PATIENT_BUNDLE" | python3 -c "import sys, json; data=json.load(sys.stdin); print(' '.join([entry['resource']['id'] for entry in data.get('entry', [])]))"))

echo "Found ${#PATIENT_IDS[@]} patients"

if [ ${#PATIENT_IDS[@]} -eq 0 ]; then
  echo "ERROR: No patients found. Please run create-100-nc-patients.sh first."
  exit 1
fi

echo ""
echo "Step 3: Creating 100 Appointments..."
echo "======================================"

for i in $(seq 1 100); do
  # Randomly select a patient
  PATIENT_IDX=$((RANDOM % ${#PATIENT_IDS[@]}))
  PATIENT_ID="${PATIENT_IDS[$PATIENT_IDX]}"

  # Randomly select a practitioner
  PRAC_IDX=$((RANDOM % ${#PRACTITIONER_IDS[@]}))
  PRAC_ID="${PRACTITIONER_IDS[$PRAC_IDX]}"

  # Get practitioner details for display
  PRAC_RESPONSE=$(curl -s "$PRACTITIONER_API/Practitioner/$PRAC_ID")
  PRAC_FAMILY=$(echo "$PRAC_RESPONSE" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data.get('name', [{}])[0].get('family', ''))")
  PRAC_GIVEN=$(echo "$PRAC_RESPONSE" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data.get('name', [{}])[0].get('given', [''])[0])")
  PRAC_SPECIALTY=$(echo "$PRAC_RESPONSE" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data.get('qualification', [{}])[0].get('code', {}).get('coding', [{}])[0].get('display', ''))")

  # Determine if past or future appointment (70% upcoming, 30% past)
  if [ $((RANDOM % 100)) -lt 70 ]; then
    # Future appointment
    START_TIME=$(generate_future_date)
    # Most future appointments are booked or pending
    if [ $((RANDOM % 100)) -lt 80 ]; then
      STATUS="booked"
    else
      STATUS="pending"
    fi
  else
    # Past appointment
    START_TIME=$(generate_past_date)
    # Past appointments can be fulfilled, cancelled, or noshow
    rand=$((RANDOM % 100))
    if [ $rand -lt 70 ]; then
      STATUS="fulfilled"
    elif [ $rand -lt 85 ]; then
      STATUS="cancelled"
    else
      STATUS="noshow"
    fi
  fi

  # Calculate end time (30 min to 2 hours)
  DURATION=$((RANDOM % 90 + 30))  # 30-120 minutes
  END_TIME=$(add_hours "$START_TIME" $((DURATION / 60)))

  APPT_TYPE=$(get_random_appt_type)
  APPT_TYPE_DISPLAY=$(get_random_appt_type_display)
  REASON=$(get_random_reason)

  APPOINTMENT_JSON=$(cat <<EOF
{
  "resourceType": "Appointment",
  "status": "$STATUS",
  "serviceType": [
    {
      "coding": [
        {
          "system": "http://terminology.hl7.org/CodeSystem/service-type",
          "code": "general",
          "display": "$APPT_TYPE_DISPLAY"
        }
      ],
      "text": "$APPT_TYPE_DISPLAY"
    }
  ],
  "appointmentType": {
    "coding": [
      {
        "system": "http://terminology.hl7.org/CodeSystem/v2-0276",
        "code": "$APPT_TYPE",
        "display": "$APPT_TYPE_DISPLAY"
      }
    ]
  },
  "reasonCode": [
    {
      "text": "$REASON"
    }
  ],
  "description": "$REASON",
  "start": "$START_TIME",
  "end": "$END_TIME",
  "minutesDuration": $DURATION,
  "participant": [
    {
      "actor": {
        "reference": "Patient/$PATIENT_ID",
        "display": "Patient $PATIENT_ID"
      },
      "required": "required",
      "status": "accepted"
    },
    {
      "actor": {
        "reference": "Practitioner/$PRAC_ID",
        "display": "Dr. $PRAC_GIVEN $PRAC_FAMILY"
      },
      "required": "required",
      "status": "accepted"
    }
  ],
  "specialty": [
    {
      "text": "$PRAC_SPECIALTY"
    }
  ]
}
EOF
)

  RESPONSE=$(curl -s -X POST "$APPOINTMENT_API/Appointment" \
    -H "Content-Type: application/json" \
    -d "$APPOINTMENT_JSON")

  APPT_ID=$(echo "$RESPONSE" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data.get('id', ''))")

  if [ -n "$APPT_ID" ]; then
    echo "✓ Appointment $i: Patient $PATIENT_ID → Dr. $PRAC_FAMILY ($STATUS on $(echo $START_TIME | cut -d'T' -f1)) - ID: $APPT_ID"
  else
    echo "✗ Failed to create Appointment $i"
  fi
done

echo ""
echo "======================================"
echo "Test Data Creation Complete!"
echo "======================================"
echo "Summary:"
echo "  - Practitioners created: ${#PRACTITIONER_IDS[@]}"
echo "  - Appointments created: 100"
echo ""
echo "You can now:"
echo "  1. View practitioners at: http://localhost:5173/providers"
echo "  2. Select a patient and view appointments at: http://localhost:5173/appointments"
echo "======================================"
