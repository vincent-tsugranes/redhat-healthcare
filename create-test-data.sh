#!/bin/bash

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Creating Test Data for Vincent Tsugranes${NC}"
echo -e "${GREEN}========================================${NC}"

# API endpoints
PATIENT_API="http://localhost:8080/fhir"
COVERAGE_API="http://localhost:8081/fhir"
CLAIMS_API="http://localhost:8082/fhir"

# Step 1: Create Patient
echo -e "\n${YELLOW}Creating patient Vincent Tsugranes...${NC}"

PATIENT_JSON=$(cat <<'EOF'
{
  "resourceType": "Patient",
  "id": "patient-vincent-001",
  "identifier": [
    {
      "system": "http://hospital.example.org/patients",
      "value": "MRN-123456"
    },
    {
      "system": "http://hl7.org/fhir/sid/us-ssn",
      "value": "123-45-6789"
    }
  ],
  "active": true,
  "name": [
    {
      "use": "official",
      "family": "Tsugranes",
      "given": ["Vincent"]
    }
  ],
  "telecom": [
    {
      "system": "phone",
      "value": "555-123-4567",
      "use": "mobile"
    },
    {
      "system": "email",
      "value": "vincent.tsugranes@example.com",
      "use": "home"
    }
  ],
  "gender": "male",
  "birthDate": "1985-06-15",
  "address": [
    {
      "use": "home",
      "type": "both",
      "line": ["123 Main Street", "Apt 4B"],
      "city": "Boston",
      "state": "MA",
      "postalCode": "02101",
      "country": "USA"
    }
  ],
  "maritalStatus": {
    "coding": [
      {
        "system": "http://terminology.hl7.org/CodeSystem/v3-MaritalStatus",
        "code": "M",
        "display": "Married"
      }
    ],
    "text": "Married"
  },
  "communication": [
    {
      "language": {
        "coding": [
          {
            "system": "urn:ietf:bcp:47",
            "code": "en-US",
            "display": "English (United States)"
          }
        ],
        "text": "English"
      },
      "preferred": true
    }
  ]
}
EOF
)

PATIENT_RESPONSE=$(curl -s -X POST "$PATIENT_API/Patient" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d "$PATIENT_JSON")

echo -e "${GREEN}✓ Patient created${NC}"
PATIENT_ID=$(echo "$PATIENT_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
echo -e "  Patient ID: $PATIENT_ID"

# Step 2: Create Coverage
echo -e "\n${YELLOW}Creating insurance coverage...${NC}"

COVERAGE_JSON=$(cat <<EOF
{
  "resourceType": "Coverage",
  "id": "coverage-vincent-001",
  "identifier": [
    {
      "system": "http://insurance.example.org/coverage",
      "value": "COV-VT-2024-001"
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
    "display": "Vincent Tsugranes"
  },
  "subscriberId": "SUB-VT-123456",
  "beneficiary": {
    "reference": "Patient/$PATIENT_ID",
    "display": "Vincent Tsugranes"
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
    },
    {
      "type": {
        "coding": [
          {
            "system": "http://terminology.hl7.org/CodeSystem/coverage-class",
            "code": "group",
            "display": "Group"
          }
        ]
      },
      "value": "EMP-12345",
      "name": "Red Hat Healthcare Employees"
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

COVERAGE_RESPONSE=$(curl -s -X POST "$COVERAGE_API/Coverage" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d "$COVERAGE_JSON")

echo -e "${GREEN}✓ Coverage created${NC}"
COVERAGE_ID=$(echo "$COVERAGE_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
echo -e "  Coverage ID: $COVERAGE_ID"

# Step 3: Create 20 Claims
echo -e "\n${YELLOW}Creating 20 synthetic claims...${NC}"

# Array of diagnoses and procedures for variety
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
  '{"code": "L70.0", "display": "Acne vulgaris"}'
  '{"code": "N39.0", "display": "Urinary tract infection"}'
  '{"code": "K59.00", "display": "Constipation"}'
  '{"code": "M79.3", "display": "Muscle pain"}'
  '{"code": "R05", "display": "Cough"}'
  '{"code": "K30", "display": "Functional dyspepsia"}'
  '{"code": "J30.1", "display": "Allergic rhinitis"}'
  '{"code": "R10.9", "display": "Abdominal pain"}'
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
  '{"code": "87880", "display": "Strep test"}'
  '{"code": "71020", "display": "Chest X-ray"}'
  '{"code": "73610", "display": "Ankle X-ray"}'
  '{"code": "81003", "display": "Urinalysis"}'
  '{"code": "90471", "display": "Immunization administration"}'
  '{"code": "99393", "display": "Preventive medicine visit"}'
  '{"code": "93000", "display": "Electrocardiogram"}'
  '{"code": "99203", "display": "Office visit - new patient"}'
  '{"code": "76856", "display": "Ultrasound examination"}'
  '{"code": "99212", "display": "Office visit - brief"}'
)

declare -a AMOUNTS=(
  "150.00" "250.00" "350.00" "450.00" "550.00"
  "175.00" "275.00" "375.00" "475.00" "575.00"
  "200.00" "300.00" "400.00" "500.00" "600.00"
  "225.00" "325.00" "425.00" "525.00" "625.00"
)

declare -a STATUSES=("active" "active" "active" "active")

for i in {1..20}; do
  # Calculate dates going back in time
  DAYS_AGO=$((i * 15))
  CLAIM_DATE=$(date -v-${DAYS_AGO}d +%Y-%m-%d 2>/dev/null || date -d "${DAYS_AGO} days ago" +%Y-%m-%d)

  DIAGNOSIS_INDEX=$((i % 20))
  PROCEDURE_INDEX=$((i % 20))
  AMOUNT_INDEX=$((i % 20))
  STATUS_INDEX=$((i % 4))

  DIAGNOSIS=${DIAGNOSES[$DIAGNOSIS_INDEX]}
  PROCEDURE=${PROCEDURES[$PROCEDURE_INDEX]}
  AMOUNT=${AMOUNTS[$AMOUNT_INDEX]}
  STATUS=${STATUSES[$STATUS_INDEX]}

  DIAG_CODE=$(echo "$DIAGNOSIS" | grep -o '"code": "[^"]*"' | cut -d'"' -f4)
  DIAG_DISPLAY=$(echo "$DIAGNOSIS" | grep -o '"display": "[^"]*"' | cut -d'"' -f4)
  PROC_CODE=$(echo "$PROCEDURE" | grep -o '"code": "[^"]*"' | cut -d'"' -f4)
  PROC_DISPLAY=$(echo "$PROCEDURE" | grep -o '"display": "[^"]*"' | cut -d'"' -f4)

  # Calculate patient responsibility (20% coinsurance + $25 copay)
  PATIENT_COPAY="25.00"
  PATIENT_COINSURANCE=$(echo "$AMOUNT * 0.20" | bc)
  PATIENT_TOTAL=$(echo "$PATIENT_COPAY + $PATIENT_COINSURANCE" | bc)
  INSURANCE_PAID=$(echo "$AMOUNT - $PATIENT_COINSURANCE - $PATIENT_COPAY" | bc)

  CLAIM_JSON=$(cat <<EOF
{
  "resourceType": "Claim",
  "id": "claim-vincent-$(printf "%03d" $i)",
  "status": "$STATUS",
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
    "display": "Vincent Tsugranes"
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
    "display": "Boston Medical Center"
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
    -H "Accept: application/json" \
    -d "$CLAIM_JSON" > /dev/null

  echo -e "${GREEN}✓ Claim $i created${NC} - $DIAG_DISPLAY (\$$AMOUNT)"
done

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}Test Data Creation Complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "\n${YELLOW}Summary:${NC}"
echo -e "  Patient: Vincent Tsugranes (ID: $PATIENT_ID)"
echo -e "  Coverage: Blue Cross Blue Shield Gold Plan (ID: $COVERAGE_ID)"
echo -e "  Claims: 20 synthetic claims created"
echo -e "\n${YELLOW}Access the portal:${NC}"
echo -e "  ${GREEN}http://localhost:8888${NC} (if running containerized)"
echo -e "  ${GREEN}http://localhost:5173${NC} (if running in dev mode)"
echo -e ""
