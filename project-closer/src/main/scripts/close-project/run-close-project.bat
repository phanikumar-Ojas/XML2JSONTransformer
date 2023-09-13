For /F "tokens=1* delims==" %%A IN (close-project.properties) DO (
    IF "%%A"=="projectId" set projectId=%%B
)


java -jar ./../project_closer.jar -p "%projectId%"
pause