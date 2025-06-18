/**
 * This script will split an openapi spec using its tags. All paths and related resources for a specific tag will be extracted and written to another file.
 * It is useful for creating test data from OpenAPI specs that are provided in a format too large to parse.
 *
 * NOTE: This requires the NPM 'yaml' package to run.
 */
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

const allComponentMapEntries = Object.entries(fullSpec.components)
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

const recursivelyReplaceRefs = (yaml, componentMapEntries) => {
    if (componentMapEntries.length === 0) return yaml;

    const refMatches = [...yaml.matchAll(REF_REGEX)].map(match => match[0]);
    const { matched, unmatched } = componentMapEntries.reduce((acc, [ref, component]) => {
        if (refMatches.includes(ref)) {
            acc.matched.push([ref, component]);
        } else {
            acc.unmatched.push([ref, component]);
        }
        return acc;
    }, { matched: [], unmatched: [] })

    if (matched.length === 0) return yaml;

    matched.filter(([ref]) => refMatches.includes(ref))
        .forEach(([ref, component]) => {
            const type = ref.split('/')[2];
            const name = ref.split('/')[3];
            if (!newSpec.components[type]) {
                newSpec.components[type] = {};
            }
            newSpec.components[type][name] = component;
        });
    const newYaml = stringify(newSpec);
    return recursivelyReplaceRefs(newYaml, unmatched);
};

const finalYaml = recursivelyReplaceRefs(stringify(newSpec), allComponentMapEntries);

fs.writeFileSync(path.join(process.cwd(), `${targetTag}.yaml`), finalYaml);
console.log('Split complete');

