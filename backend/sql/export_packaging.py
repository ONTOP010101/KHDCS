import subprocess
proc = subprocess.run(
    ['mysql', '-u', 'root', '-p123456', 'photo_management',
     '--default-character-set=utf8mb4', '--skip-column-names', '--batch',
     '-e', "SELECT CONCAT('INSERT INTO packaging_methods (id, code, name, name_en, create_time, update_time, deleted) VALUES (', id, ',', QUOTE(code), ',', QUOTE(name), ',', QUOTE(COALESCE(name_en,'''')), ',', QUOTE(create_time), ',', QUOTE(update_time), ',', deleted, ');') FROM packaging_methods ORDER BY CAST(code AS UNSIGNED)"],
    capture_output=True, text=False
)
output = proc.stdout.decode('utf-8')
with open(r'd:\客户端测试\backend\sql\packaging-methods-seed.sql', 'w', encoding='utf-8') as f:
    f.write("SET NAMES utf8mb4;\n")
    f.write(output)
print(f"Done, {output.count(chr(10))} lines.")
