import { parse, stringify } from 'yaml'
import path from 'path';
import fs from 'fs';

const METHODS = ['get', 'post', 'put', 'patch', 'delete', 'options', 'head', 'trace'];

const targetTag = process.argv[2];
if (!targetTag) throw new Error('Missing target tag argument');
console.log('Extracting tag', targetTag);

const originalYaml = fs.readFileSync(path.join(process.cwd(), 'github-actions-v3.yaml'), 'utf8');
const fullSpec = parse(originalYaml);

const matchingPathEntries = Object.entries(fullSpec.paths)
    .filter(([uri, pathConfig]) => {
        const isMatch = pathConfig.tags?.includes(targetTag);
        if (!isMatch) {
            return Object.entries(pathConfig)
                .filter(([method, methodConfig]) => methodConfig.tags?.includes(targetTag))
        }
        return false;
    })
    .map(([uri, pathConfig]) => {
        const newPathConfig = Object.entries(pathConfig)
            .filter(([method, methodConfig]) => !METHODS.includes(method) || methodConfig.tags?.includes(targetTag));
        return [uri, newPathConfig];
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
};
console.log(newSpec);