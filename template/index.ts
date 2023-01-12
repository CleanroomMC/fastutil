import {} from 'fs';
import {renderFile} from 'template-file';
import {genTypedTemplate, TemplatingProps, templatingTypes, TypeTemplating} from "./constants";

const tmplDir = './templates'
const outputDir = '../src/main/java/com/cleanroommc/fastutil';

type SourceTemplatingSet = {
    [key: string]: (tmpl: TemplatingProps) => string
};



const files: SourceTemplatingSet = {
    "ConcurrentArrayList.tmpl": (tmpl: TemplatingProps) => `Concurrent${tmpl['capitalizedTypeName']}ArrayList`
};

(async () => {
    for (const [fileName, className] of Object.entries(files)) {
        let filePath = `${tmplDir}/${fileName}`;

        for (const typeName of templatingTypes) {
            let typedTemplate = genTypedTemplate(typeName, className);

            console.log(await renderFile(filePath, typedTemplate));
        }
    }
})()
