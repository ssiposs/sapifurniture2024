#!/bin/bash

echo "------------------------------------------"
echo "🚀 Autentikációs tesztek indítása..."
echo "------------------------------------------"

# Jogosultság adása a Maven Wrappernek
chmod +x mvnw

# A projekt tisztítása és CSAK az autentikációhoz kapcsolódó tesztek futtatása
# Ha az összes tesztet akarod: ./mvnw clean test
./mvnw clean test -Dtest=AppUserRepositoryTest,UserDetailsServiceImplTest,AuthControllerTest,AuthIntegrationTest,AuthComponentTest

# Ellenőrizzük a kilépési kódot
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ SIKER: Minden autentikációs teszt átment!"
    echo "------------------------------------------"
else
    echo ""
    echo "❌ HIBA: Néhány teszt elbukott!"
    echo "Ellenőrizd a jelentést itt: target/surefire-reports/index.html"
    echo "------------------------------------------"
    exit 1
fi