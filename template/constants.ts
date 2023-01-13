export const templatingTypes = ['boolean', 'char', 'byte', 'short', 'int', 'long', 'float', 'double'] as const;
export type TemplatingTypes = typeof templatingTypes[number];

export const wrapperTypes: {[key in TemplatingTypes]: string} = {
    'boolean': 'Boolean',
    'char': 'Character',
    'byte': 'Byte',
    'short': 'Short',
    'int': 'Integer',
    'long': 'Long',
    'float': 'Float',
    'double': 'Double'
};

export const defaultValues: {[key in TemplatingTypes]: string} = {
    'boolean': 'false',
    'char': '\\u0000',
    'byte': '0',
    'short': '0',
    'int': '0',
    'long': '0L',
    'float': '0F',
    'double': '0D'
};

export type TemplatingProps = {
    [key: string]: string
};

export type TypeTemplating = (props: TemplatingProps) => string;

export const commonTemplate: TemplatingProps = {
    packagePath: 'com.cleanroommc.fastutil'
};

function capitalize(literal: string): string {
    return literal[0].toUpperCase() + literal.slice(1);
}

export function genTypedTemplate(type: TemplatingTypes, classNameTemplating: TypeTemplating): TemplatingProps {
    let typedTemplate = commonTemplate;

    typedTemplate['typePackagePath'] = `${typedTemplate['packagePath']}.${type}`;
    typedTemplate['primitiveTypeName'] = type;
    typedTemplate['capitalizedPrimitiveTypeName'] = capitalize(type);
    typedTemplate['className'] = classNameTemplating(typedTemplate);
    typedTemplate['wrapperClassName'] = wrapperTypes[type];
    typedTemplate['defaultValue'] = defaultValues[type];

    return typedTemplate;
}
