# 1. Задаем имя тенанта (можете изменить внутри кавычек на свое)
$tenant_name = "my-duffy-tenant"

# 2. Генерируем SSH-ключи (без пароля, в текущую папку)
ssh-keygen -t rsa -b 3072 -f "./${tenant_name}_id_rsa" -N '""' -C "${tenant_name}@duffy-ci"

# 3. Считываем созданный публичный ключ
$pub_key = Get-Content "./${tenant_name}_id_rsa.pub" -Raw

# 4. Выводим готовую команду для сервера Duffy
Write-Host ""
Write-Host "=== СКОПИРУЙТЕ СТРОКУ НИЖЕ И ВЫПОЛНИТЕ НА СЕРВЕРЕ DUFFY ===" -ForegroundColor Green
Write-Host ""
Write-Host "duffy admin create-tenant $tenant_name `"$($pub_key.Trim())`""
Write-Host ""
Write-Host "=======================================================" -ForegroundColor Green