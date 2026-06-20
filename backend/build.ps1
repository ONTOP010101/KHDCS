$pomPath = "d:\客户端测试\backend\pom.xml"
[xml]$pom = Get-Content $pomPath

$ns = New-Object Xml.XmlNamespaceManager($pom.NameTable)
$ns.AddNamespace("m", "http://maven.apache.org/POM/4.0.0")

$versionNode = $pom.SelectSingleNode("/m:project/m:version", $ns)
$oldVersion = $versionNode.InnerText
Write-Host "old: $oldVersion"

$parts = $oldVersion -split '\.'
$parts[2] = [int]$parts[2] + 1
$newVersion = $parts -join '.'
$versionNode.InnerText = $newVersion
$pom.Save($pomPath)
Write-Host "new: $newVersion"

Write-Host "building..." -ForegroundColor Yellow
$jarFile = "d:\客户端测试\backend\target\photo-management-$newVersion.jar"
mvn clean package -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host "SUCCESS: photo-management-$newVersion.jar" -ForegroundColor Green

    # kill old backend
    $procs = Get-WmiObject Win32_Process -Filter "Name='java.exe'"
    foreach ($p in $procs) {
        if ($p.CommandLine -match 'photo-management') {
            Write-Host "killing PID $($p.ProcessId)..."
            Stop-Process -Id $p.ProcessId -Force -ErrorAction SilentlyContinue
        }
    }

    Write-Host "starting backend..." -ForegroundColor Green
    $args = @('-jar', $jarFile)
    Start-Process -FilePath java -ArgumentList $args -WorkingDirectory "d:\客户端测试\backend\target"
    Write-Host "done: http://localhost:8080"
}
else {
    Write-Host "FAILED!" -ForegroundColor Red
    $versionNode.InnerText = $oldVersion
    $pom.Save($pomPath)
    Write-Host "version rolled back to $oldVersion"
}
