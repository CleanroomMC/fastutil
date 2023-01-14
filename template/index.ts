import {writeFileSync, mkdirSync, existsSync} from 'fs';
import {renderFile} from 'template-file';
import {genTypedTemplate, packagePath, TemplatingProps, templatingTypes, TypeTemplating} from "./constants";

const tmplDir = './templates'
const outputDir = '../src/main/java/com/cleanroommc/fastutil';

type SourceTemplatingSet = {
    [key: string]: (tmpl: TemplatingProps) => string
};


const files: SourceTemplatingSet = {
    "AbstractConcurrentList.tmpl.java": (tmpl: TemplatingProps) => `AbstractConcurrent${tmpl['capitalizedPrimitiveTypeName']}List`
};

(async () => {
    for (const type of templatingTypes) {
        let dirPath = `../src/main/java/${packagePath.replaceAll('.', '/')}/${type}s`;

        if (!existsSync(dirPath)) {
            mkdirSync(dirPath);
        }
    }

    for (const [fileName, className] of Object.entries(files)) {
        let filePath = `${tmplDir}/${fileName}`;

        for (const typeName of templatingTypes) {
            let typedTemplate = genTypedTemplate(typeName, className);
            let outputFilePath = `../src/main/java/${typedTemplate['typePackageDirPath']}s/${typedTemplate['className']}.java`
            let renderedSource = await renderFile(filePath, typedTemplate);

            writeFileSync(outputFilePath, renderedSource);
        }
    }
})()
