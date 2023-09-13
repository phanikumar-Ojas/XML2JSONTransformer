For /F "tokens=1* delims==" %%A IN (generate-image-sheet.properties) DO (
    IF "%%A"=="outputDir" set outputDir=%%B
    IF "%%A"=="projectId" set projectId=%%B
)

echo "%outputDir%"
java -jar ./../image_importer.jar -g -o "%outputDir%" -p "%projectId%"
pause