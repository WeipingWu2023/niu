import {mkdir,copyFile,cp,access} from 'node:fs/promises';
const target='android/app/src/main/assets/www';
await mkdir(target,{recursive:true});
for(const file of ['index.html','app.js','core.js','style.css']) await copyFile(file,`${target}/${file}`);
await cp('assets',`${target}/assets`,{recursive:true});
await access('android/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png');
console.log('Android离线资源已准备');
