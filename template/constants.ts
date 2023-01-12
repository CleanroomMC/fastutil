export const templatingTypes = ['boolean'];
export type TemplatingTypes = typeof templatingTypes[number];

export type TemplatingProps = {
    [key: string]: string
};

export type TypeTemplating = (props: TemplatingProps) => string;

export const commonTemplate: TemplatingProps = {
    packagePath: 'com.cleanroommc.fastutil'
};

export function genTypedTemplate(type: TemplatingTypes, classNameTemplating: TypeTemplating): TemplatingProps {
    let typedTemplate = commonTemplate;

    typedTemplate['typePackagePath'] = `${typedTemplate['packagePath']}.${type}`;
    typedTemplate['typeName'] = type;
    typedTemplate['className'] = classNameTemplating(typedTemplate);

    return typedTemplate;
}
