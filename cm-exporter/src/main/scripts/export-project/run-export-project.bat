For /F "tokens=1* delims==" %%A IN (export-project.properties) DO (
    IF "%%A"=="projectId" set projectId=%%B
)


java -jar ./../mfssync.jar -p "%projectId%"
java -jar ./../cm_exporter.jar -p "%projectId%" %*
pause