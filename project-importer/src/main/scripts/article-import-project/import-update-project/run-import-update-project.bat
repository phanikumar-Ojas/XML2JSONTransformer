For /F "tokens=1* delims==" %%A IN (import-update-project.properties) DO (
    IF "%%A"=="projectId" set projectId=%%B
    IF "%%A"=="projectInputSheet" set projectInputSheet=%%B
)


java -jar ./../update_project_importer.jar -i -s "%projectInputSheet%" -p "%projectId%"
pause