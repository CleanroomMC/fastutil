export const templatingTypes = ['boolean', 'byte', 'short', 'int', 'long', 'float', 'double'] as const;
export type TemplatingTypes = typeof templatingTypes[number];

export const wrapperTypes: {[key in TemplatingTypes]: string} = {
    'boolean': 'Boolean',
    'byte': 'Byte',
    'short': 'Short',
    'int': 'Integer',
    'long': 'Long',
    'float': 'Float',
    'double': 'Double'
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

    return typedTemplate;
}
