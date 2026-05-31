$env:MYSQL_PWD = "123456"
$hex = "0123456789abcdef"
$count = 0
for ($i = 0; $i -lt 16; $i++) {
    for ($j = 0; $j -lt 16; $j++) {
        $prefix = "$($hex[$i])$($hex[$j])"
        $sql = "ALTER TABLE images_$prefix ADD INDEX idx_dhash_$prefix (dhash)"
        mysql -u root photo_db -e $sql 2>&1 | Out-Null
        $count++
    }
}
Write-Host "Created $count indexes"
