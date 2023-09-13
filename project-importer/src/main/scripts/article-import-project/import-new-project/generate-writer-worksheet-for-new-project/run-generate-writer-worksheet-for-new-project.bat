For /F "tokens=1* delims==" %%A IN (generate-writer-worksheet-for-new-project.properties) DO (
    IF "%%A"=="projectId" set projectId=%%B
    IF "%%A"=="outputDir" set outputDir=%%B
)


java -jar ./../new_project_importer.jar -gw -o "%outputDir%" -p "%projectId%"
pause