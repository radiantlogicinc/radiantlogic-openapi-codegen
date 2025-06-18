import { parse, stringify } from 'yaml'
import path from 'path';
import fs from 'fs';

const METHODS = ['get', 'post', 'put', 'patch', 'delete', 'options', 'head', 'trace'];
const REF_REGEX = /(#\/components\/([^'"]*))/g;

const targetTag = process.argv[2];
if (!targetTag) throw new Error('Missing target tag argument');
console.log('Extracting tag', targetTag);

const originalYaml = fs.readFileSync(path.join(process.cwd(), 'github-v3.yaml'), 'utf8');
const fullSpec = parse(originalYaml);

const componentMapEntries = Object.entries(fullSpec.components)
    .flatMap(([componentType, componentsForType]) => {
        return Object.entries(componentsForType)
            .map(([componentName, component]) => [`#/components/${componentType}/${componentName}`, component]);
    });

const matchingPathEntries = Object.entries(fullSpec.paths)
    .filter(([uri, pathConfig]) => {
        const isMatch = pathConfig.tags?.includes(targetTag);
        if (!isMatch) {
            return Object.entries(pathConfig)
                .filter(([method, methodConfig]) => methodConfig.tags?.includes(targetTag))
                .length > 0;
        }
        return false;
    })
    .map(([uri, pathConfig]) => {
        const newPathConfigEntries = Object.entries(pathConfig)
            .filter(([method, methodConfig]) => !METHODS.includes(method) || methodConfig.tags?.includes(targetTag));
        return [uri, Object.fromEntries(newPathConfigEntries)];
    });
const matchingPaths = Object.fromEntries(matchingPathEntries);

const matchingTags = fullSpec.tags.filter(tag => tag.name === targetTag);
const newSpec = {
    openapi: fullSpec.openapi,
    info: {
        ...fullSpec.info,
        title: `${fullSpec.info.title} - ${targetTag}`,
    },
    tags: matchingTags,
    paths: matchingPaths,
    components: {}
};
const newYamlV1 = stringify(newSpec);

const refMatches = [...newYamlV1.matchAll(REF_REGEX)].map(match => match[0])
componentMapEntries.filter(([ref]) => refMatches.includes(ref))
    .forEach(([ref, component]) => {
        const type = ref.split('/')[2];
        const name = ref.split('/')[3];
        if (!newSpec.components[type]) {
            newSpec.components[type] = {};
        }
        newSpec.components[type][name] = component;
    });
const newYamlV2 = stringify(newSpec);



fs.writeFileSync(path.join(process.cwd(), `${targetTag}.yaml`), newYamlV2);

