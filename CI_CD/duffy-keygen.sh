# 1. Генерируем ключи без лишних вопросов (пароль пустой, имя тенанта по умолчанию my-duffy-tenant)
tenant_name="my-duffy-tenant"
ssh-keygen -t rsa -b 3072 -f "./${tenant_name}_id_rsa" -N "" -C "${tenant_name}@duffy-ci"

# 2. Выводим готовую команду для сервера Duffy
echo -e "\n\n=== СКОПИРУЙТЕ СТРОКУ НИЖЕ И ВЫПОЛНИТЕ НА СЕРВЕРЕ DUFFY ===\n"
echo "duffy admin create-tenant ${tenant_name} \"$(cat ./${tenant_name}_id_rsa.pub)\""
echo -e "\n=======================================================\n"