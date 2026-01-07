#!/bin/bash

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Creating 100 NC Patients with Full Data${NC}"
echo -e "${GREEN}========================================${NC}"

# API endpoints
PATIENT_API="http://localhost:8080/fhir"
COVERAGE_API="http://localhost:8081/fhir"
CLAIMS_API="http://localhost:8082/fhir"

# NC Cities and Counties (parallel arrays)
declare -a NC_CITY_NAMES=(
  "Charlotte" "Raleigh" "Durham" "Greensboro" "Winston-Salem"
  "Fayetteville" "Cary" "Wilmington" "Asheville" "Chapel Hill"
)

declare -a NC_CITY_COUNTIES=(
  "Mecklenburg" "Wake" "Durham" "Guilford" "Forsyth"
  "Cumberland" "Wake" "New Hanover" "Buncombe" "Orange"
)

# Arrays for random data generation
declare -a FIRST_NAMES=(
  "James" "Mary" "John" "Patricia" "Robert" "Jennifer" "Michael" "Linda"
  "William" "Elizabeth" "David" "Barbara" "Richard" "Susan" "Joseph" "Jessica"
  "Thomas" "Sarah" "Christopher" "Karen" "Charles" "Nancy" "Daniel" "Lisa"
  "Matthew" "Betty" "Anthony" "Margaret" "Mark" "Sandra" "Donald" "Ashley"
  "Steven" "Kimberly" "Paul" "Emily" "Andrew" "Donna" "Joshua" "Michelle"
  "Kenneth" "Carol" "Kevin" "Amanda" "Brian" "Dorothy" "George" "Melissa"
  "Timothy" "Deborah" "Ronald" "Stephanie" "Edward" "Rebecca" "Jason" "Sharon"
  "Jeffrey" "Laura" "Ryan" "Cynthia" "Jacob" "Kathleen" "Gary" "Amy"
  "Nicholas" "Angela" "Eric" "Shirley" "Jonathan" "Anna" "Stephen" "Brenda"
  "Larry" "Pamela" "Justin" "Emma" "Scott" "Nicole" "Brandon" "Helen"
)

declare -a LAST_NAMES=(
  "Smith" "Johnson" "Williams" "Brown" "Jones" "Garcia" "Miller" "Davis"
  "Rodriguez" "Martinez" "Hernandez" "Lopez" "Gonzalez" "Wilson" "Anderson" "Thomas"
  "Taylor" "Moore" "Jackson" "Martin" "Lee" "Thompson" "White" "Harris"
  "Sanchez" "Clark" "Ramirez" "Lewis" "Robinson" "Walker" "Young" "Allen"
  "King" "Wright" "Scott" "Torres" "Nguyen" "Hill" "Flores" "Green"
  "Adams" "Nelson" "Baker" "Hall" "Rivera" "Campbell" "Mitchell" "Carter"
  "Roberts" "Gomez" "Phillips" "Evans" "Turner" "Diaz" "Parker" "Cruz"
)

declare -a STREET_NAMES=(
  "Main St" "Oak Ave" "Maple Dr" "Cedar Ln" "Pine Rd" "Elm St"
  "Washington Ave" "Park Blvd" "Lake Dr" "Hill St" "Forest Rd" "River Ave"
)

declare -a DIAGNOSES=(
  '{"code": "J06.9", "display": "Acute upper respiratory infection"}'
  '{"code": "I10", "display": "Essential hypertension"}'
  '{"code": "E11.9", "display": "Type 2 diabetes mellitus"}'
  '{"code": "M54.5", "display": "Low back pain"}'
  '{"code": "J45.909", "display": "Unspecified asthma"}'
  '{"code": "K21.9", "display": "Gastro-esophageal reflux disease"}'
  '{"code": "E78.5", "display": "Hyperlipidemia"}'
  '{"code": "F41.1", "display": "Generalized anxiety disorder"}'
  '{"code": "M25.511", "display": "Pain in right shoulder"}'
  '{"code": "H52.13", "display": "Myopia"}'
  '{"code": "J02.9", "display": "Acute pharyngitis"}'
  '{"code": "R51", "display": "Headache"}'
  '{"code": "N39.0", "display": "Urinary tract infection"}'
  '{"code": "K30", "display": "Functional dyspepsia"}'
  '{"code": "J30.1", "display": "Allergic rhinitis"}'
)

declare -a PROCEDURES=(
  '{"code": "99213", "display": "Office visit - established patient"}'
  '{"code": "99214", "display": "Office visit - detailed"}'
  '{"code": "80053", "display": "Comprehensive metabolic panel"}'
  '{"code": "85025", "display": "Complete blood count"}'
  '{"code": "80061", "display": "Lipid panel"}'
  '{"code": "83036", "display": "Hemoglobin A1C"}'
  '{"code": "36415", "display": "Venipuncture"}'
  '{"code": "94640", "display": "Nebulizer treatment"}'
  '{"code": "97110", "display": "Therapeutic exercises"}'
  '{"code": "92004", "display": "Eye examination"}'
)

# Generate random SSN-like number
gen_ssn() {
  printf "%03d-%02d-%04d" $((RANDOM % 900 + 100)) $((RANDOM % 90 + 10)) $((RANDOM % 9000 + 1000))
}

# Generate random birthdate (ages 18-85)
gen_birthdate() {
  local age=$((RANDOM % 68 + 18))
  local year=$((2024 - age))
  local month=$(printf "%02d" $((RANDOM % 12 + 1)))
  local day=$(printf "%02d" $((RANDOM % 28 + 1)))
  echo "$year-$month-$day"
}

# Generate random phone
gen_phone() {
  printf "555-%03d-%04d" $((RANDOM % 900 + 100)) $((RANDOM % 9000 + 1000))
}

# Get random array element by name
get_random_first_name() {
  local idx=$((RANDOM % ${#FIRST_NAMES[@]}))
  echo "${FIRST_NAMES[$idx]}"
}

get_random_last_name() {
  local idx=$((RANDOM % ${#LAST_NAMES[@]}))
  echo "${LAST_NAMES[$idx]}"
}

get_random_street() {
  local idx=$((RANDOM % ${#STREET_NAMES[@]}))
  echo "${STREET_NAMES[$idx]}"
}

# Get random city index
get_random_city_idx() {
  echo $((RANDOM % ${#NC_CITY_NAMES[@]}))
}

# Generate random claim amount
gen_claim_amount() {
  local amounts=(150 200 250 300 350 400 450 500 550 600 650 700 750 800)
  local idx=$((RANDOM % ${#amounts[@]}))
  echo "${amounts[$idx]}.00"
}

# Generate random claim status (70% paid, 20% pending, 10% rejected)
gen_claim_status() {
  local rand=$((RANDOM % 100))
  if [ $rand -lt 70 ]; then
    echo "active"  # Paid/active claims
  elif [ $rand -lt 90 ]; then
    echo "draft"   # Pending claims
  else
    echo "cancelled"  # Rejected claims
  fi
}

# Main loop to create 100 patients
for i in {1..100}; do
  echo -e "\n${YELLOW}Creating Patient $i/100...${NC}"

  # Generate patient data
  FIRST_NAME=$(get_random_first_name)
  LAST_NAME=$(get_random_last_name)
  GENDER=$([ $((RANDOM % 2)) -eq 0 ] && echo "male" || echo "female")
  BIRTHDATE=$(gen_birthdate)
  SSN=$(gen_ssn)
  PHONE=$(gen_phone)
  EMAIL="$(echo $FIRST_NAME | tr '[:upper:]' '[:lower:]').$(echo $LAST_NAME | tr '[:upper:]' '[:lower:]')@example.com"
  CITY_IDX=$(get_random_city_idx)
  CITY_DISPLAY="${NC_CITY_NAMES[$CITY_IDX]}"
  COUNTY="${NC_CITY_COUNTIES[$CITY_IDX]}"
  STREET_NUM=$((RANDOM % 9000 + 1000))
  STREET=$(get_random_street)
  ZIP=$((RANDOM % 10000 + 27000))

  PATIENT_ID="patient-$(printf "%03d" $i)"
  MRN="MRN-$(printf "%06d" $i)"

  # Create Patient
  PATIENT_JSON=$(cat <<EOF
{
  "resourceType": "Patient",
  "id": "$PATIENT_ID",
  "identifier": [
    {
      "system": "http://hospital.example.org/patients",
      "value": "$MRN"
    },
    {
      "system": "http://hl7.org/fhir/sid/us-ssn",
      "value": "$SSN"
    }
  ],
  "active": true,
  "name": [
    {
      "use": "official",
      "family": "$LAST_NAME",
      "given": ["$FIRST_NAME"]
    }
  ],
  "telecom": [
    {
      "system": "phone",
      "value": "$PHONE",
      "use": "mobile"
    },
    {
      "system": "email",
      "value": "$EMAIL",
      "use": "home"
    }
  ],
  "gender": "$GENDER",
  "birthDate": "$BIRTHDATE",
  "address": [
    {
      "use": "home",
      "type": "both",
      "line": ["$STREET_NUM $STREET"],
      "city": "$CITY_DISPLAY",
      "state": "NC",
      "postalCode": "$ZIP",
      "country": "USA"
    }
  ]
}
EOF
)

  curl -s -X POST "$PATIENT_API/Patient" \
    -H "Content-Type: application/json" \
    -d "$PATIENT_JSON" > /dev/null

  echo -e "${GREEN}✓ Patient created:${NC} $FIRST_NAME $LAST_NAME ($CITY_DISPLAY, $COUNTY County)"

  # Create Coverage with OOP Max
  COVERAGE_ID="coverage-$(printf "%03d" $i)"
  SUBSCRIBER_ID="SUB-$(printf "%06d" $i)"

  COVERAGE_JSON=$(cat <<EOF
{
  "resourceType": "Coverage",
  "id": "$COVERAGE_ID",
  "identifier": [
    {
      "system": "http://insurance.example.org/coverage",
      "value": "$SUBSCRIBER_ID"
    }
  ],
  "status": "active",
  "type": {
    "coding": [
      {
        "system": "http://terminology.hl7.org/CodeSystem/v3-ActCode",
        "code": "HIP",
        "display": "health insurance plan policy"
      }
    ],
    "text": "Health Insurance Plan"
  },
  "subscriber": {
    "reference": "Patient/$PATIENT_ID",
    "display": "$FIRST_NAME $LAST_NAME"
  },
  "subscriberId": "$SUBSCRIBER_ID",
  "beneficiary": {
    "reference": "Patient/$PATIENT_ID",
    "display": "$FIRST_NAME $LAST_NAME"
  },
  "relationship": {
    "coding": [
      {
        "system": "http://terminology.hl7.org/CodeSystem/subscriber-relationship",
        "code": "self",
        "display": "Self"
      }
    ],
    "text": "Self"
  },
  "period": {
    "start": "2024-01-01",
    "end": "2024-12-31"
  },
  "payor": [
    {
      "reference": "Organization/insurance-company-001",
      "display": "Blue Cross Blue Shield"
    }
  ],
  "class": [
    {
      "type": {
        "coding": [
          {
            "system": "http://terminology.hl7.org/CodeSystem/coverage-class",
            "code": "plan",
            "display": "Plan"
          }
        ]
      },
      "value": "Gold Plan",
      "name": "Gold Comprehensive Coverage"
    }
  ],
  "network": "Preferred Provider Network",
  "costToBeneficiary": [
    {
      "type": {
        "coding": [
          {
            "system": "http://terminology.hl7.org/CodeSystem/coverage-copay-type",
            "code": "copay",
            "display": "Copay"
          }
        ],
        "text": "Copay"
      },
      "valueMoney": {
        "value": 25.00,
        "currency": "USD"
      }
    },
    {
      "type": {
        "coding": [
          {
            "system": "http://terminology.hl7.org/CodeSystem/coverage-copay-type",
            "code": "coinsure",
            "display": "Co-insurance"
          }
        ],
        "text": "Coinsurance"
      },
      "valueQuantity": {
        "value": 20,
        "unit": "%"
      }
    },
    {
      "type": {
        "coding": [
          {
            "system": "http://terminology.hl7.org/CodeSystem/coverage-copay-type",
            "code": "gpay",
            "display": "Out of Pocket Maximum"
          }
        ],
        "text": "Out of Pocket Maximum"
      },
      "valueMoney": {
        "value": 6000.00,
        "currency": "USD"
      }
    }
  ]
}
EOF
)

  curl -s -X POST "$COVERAGE_API/Coverage" \
    -H "Content-Type: application/json" \
    -d "$COVERAGE_JSON" > /dev/null

  echo -e "${GREEN}✓ Coverage created${NC} with \$6,000 OOP max"

  # Create random number of claims (5-15 per patient)
  NUM_CLAIMS=$((RANDOM % 11 + 5))

  for c in $(seq 1 $NUM_CLAIMS); do
    CLAIM_ID="claim-$(printf "%03d" $i)-$(printf "%02d" $c)"
    DAYS_AGO=$((RANDOM % 365 + 1))
    CLAIM_DATE=$(date -v-${DAYS_AGO}d +%Y-%m-%d 2>/dev/null || date -d "${DAYS_AGO} days ago" +%Y-%m-%d)

    DIAG_IDX=$((RANDOM % ${#DIAGNOSES[@]}))
    PROC_IDX=$((RANDOM % ${#PROCEDURES[@]}))

    DIAGNOSIS=${DIAGNOSES[$DIAG_IDX]}
    PROCEDURE=${PROCEDURES[$PROC_IDX]}
    AMOUNT=$(gen_claim_amount)
    CLAIM_STATUS=$(gen_claim_status)

    DIAG_CODE=$(echo "$DIAGNOSIS" | grep -o '"code": "[^"]*"' | cut -d'"' -f4)
    DIAG_DISPLAY=$(echo "$DIAGNOSIS" | grep -o '"display": "[^"]*"' | cut -d'"' -f4)
    PROC_CODE=$(echo "$PROCEDURE" | grep -o '"code": "[^"]*"' | cut -d'"' -f4)
    PROC_DISPLAY=$(echo "$PROCEDURE" | grep -o '"display": "[^"]*"' | cut -d'"' -f4)

    # Calculate patient responsibility
    PATIENT_COPAY="25.00"
    PATIENT_COINSURANCE=$(echo "$AMOUNT * 0.20" | bc)
    PATIENT_TOTAL=$(echo "$PATIENT_COPAY + $PATIENT_COINSURANCE" | bc)
    INSURANCE_PAID=$(echo "$AMOUNT - $PATIENT_COINSURANCE - $PATIENT_COPAY" | bc)

    CLAIM_JSON=$(cat <<EOF
{
  "resourceType": "Claim",
  "id": "$CLAIM_ID",
  "status": "$CLAIM_STATUS",
  "type": {
    "coding": [
      {
        "system": "http://terminology.hl7.org/CodeSystem/claim-type",
        "code": "professional",
        "display": "Professional"
      }
    ],
    "text": "Professional"
  },
  "use": "claim",
  "patient": {
    "reference": "Patient/$PATIENT_ID",
    "display": "$FIRST_NAME $LAST_NAME"
  },
  "billablePeriod": {
    "start": "$CLAIM_DATE",
    "end": "$CLAIM_DATE"
  },
  "created": "${CLAIM_DATE}T10:00:00Z",
  "insurer": {
    "reference": "Organization/insurance-company-001",
    "display": "Blue Cross Blue Shield"
  },
  "provider": {
    "reference": "Organization/hospital-001",
    "display": "$CITY_DISPLAY Medical Center"
  },
  "priority": {
    "coding": [
      {
        "system": "http://terminology.hl7.org/CodeSystem/processpriority",
        "code": "normal",
        "display": "Normal"
      }
    ]
  },
  "diagnosis": [
    {
      "sequence": 1,
      "diagnosisCodeableConcept": {
        "coding": [
          {
            "system": "http://hl7.org/fhir/sid/icd-10-cm",
            "code": "$DIAG_CODE",
            "display": "$DIAG_DISPLAY"
          }
        ],
        "text": "$DIAG_DISPLAY"
      },
      "type": [
        {
          "coding": [
            {
              "system": "http://terminology.hl7.org/CodeSystem/ex-diagnosistype",
              "code": "principal",
              "display": "Principal Diagnosis"
            }
          ]
        }
      ]
    }
  ],
  "procedure": [
    {
      "sequence": 1,
      "date": "${CLAIM_DATE}T10:00:00Z",
      "procedureCodeableConcept": {
        "coding": [
          {
            "system": "http://www.ama-assn.org/go/cpt",
            "code": "$PROC_CODE",
            "display": "$PROC_DISPLAY"
          }
        ],
        "text": "$PROC_DISPLAY"
      }
    }
  ],
  "insurance": [
    {
      "sequence": 1,
      "focal": true,
      "coverage": {
        "reference": "Coverage/$COVERAGE_ID",
        "display": "Blue Cross Blue Shield - Gold Plan"
      }
    }
  ],
  "item": [
    {
      "sequence": 1,
      "productOrService": {
        "coding": [
          {
            "system": "http://www.ama-assn.org/go/cpt",
            "code": "$PROC_CODE",
            "display": "$PROC_DISPLAY"
          }
        ],
        "text": "$PROC_DISPLAY"
      },
      "servicedDate": "$CLAIM_DATE",
      "quantity": {
        "value": 1
      },
      "unitPrice": {
        "value": $AMOUNT,
        "currency": "USD"
      },
      "net": {
        "value": $AMOUNT,
        "currency": "USD"
      },
      "adjudication": [
        {
          "category": {
            "coding": [
              {
                "system": "http://terminology.hl7.org/CodeSystem/adjudication",
                "code": "submitted",
                "display": "Submitted Amount"
              }
            ]
          },
          "amount": {
            "value": $AMOUNT,
            "currency": "USD"
          }
        },
        {
          "category": {
            "coding": [
              {
                "system": "http://terminology.hl7.org/CodeSystem/adjudication",
                "code": "copay",
                "display": "Copay"
              }
            ]
          },
          "amount": {
            "value": $PATIENT_COPAY,
            "currency": "USD"
          }
        },
        {
          "category": {
            "coding": [
              {
                "system": "http://terminology.hl7.org/CodeSystem/adjudication",
                "code": "eligible",
                "display": "Patient Responsibility"
              }
            ]
          },
          "amount": {
            "value": $PATIENT_TOTAL,
            "currency": "USD"
          }
        },
        {
          "category": {
            "coding": [
              {
                "system": "http://terminology.hl7.org/CodeSystem/adjudication",
                "code": "benefit",
                "display": "Insurance Paid"
              }
            ]
          },
          "amount": {
            "value": $INSURANCE_PAID,
            "currency": "USD"
          }
        }
      ]
    }
  ],
  "total": {
    "value": $AMOUNT,
    "currency": "USD"
  }
}
EOF
)

    curl -s -X POST "$CLAIMS_API/Claim" \
      -H "Content-Type: application/json" \
      -d "$CLAIM_JSON" > /dev/null
  done

  echo -e "${GREEN}✓ $NUM_CLAIMS claims created${NC}"
  echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
done

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}All 100 NC Patients Created!${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "\n${YELLOW}Access the portal:${NC}"
echo -e "  ${GREEN}http://localhost:8888${NC}"
