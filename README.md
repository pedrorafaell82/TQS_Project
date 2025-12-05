# Sound Shop
TQS Project

## dados de teste 
# Criar user owner (ID 1)
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "owner@test.com",
    "name": "João Silva",
    "passwordHash": "hash123",
    "role": "OWNER"
  }'

# Criar user renter (ID 2)
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "renter@test.com",
    "name": "Maria Santos",
    "passwordHash": "hash456",
    "role": "RENTER"
  }'

# Criar instrumento 1
curl -X POST http://localhost:8080/api/instruments \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Fender Stratocaster",
    "description": "Classic electric guitar",
    "category": "GUITAR",
    "brand": "Fender",
    "conditionGrade": "EXCELLENT",
    "dailyPrice": 25.00,
    "ownerId": 1
  }'

# Criar instrumento 2
curl -X POST http://localhost:8080/api/instruments \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Pearl Export Drums",
    "description": "Complete drum kit",
    "category": "DRUMS",
    "brand": "Pearl",
    "conditionGrade": "GOOD",
    "dailyPrice": 50.00,
    "ownerId": 1
  }'

# Criar instrumento 3
curl -X POST http://localhost:8080/api/instruments \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Yamaha P-125",
    "description": "Digital piano 88 keys",
    "category": "KEYBOARD",
    "brand": "Yamaha",
    "conditionGrade": "EXCELLENT",
    "dailyPrice": 40.00,
    "ownerId": 1
  }'
