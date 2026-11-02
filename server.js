import http from 'node:http';
import {readFile} from 'node:fs/promises';
import path from 'node:path';
const root=process.cwd();
http.createServer(async(req,res)=>{try{const name=decodeURIComponent(new URL(req.url,'http://localhost').pathname);const relative=name==='/'?'index.html':name.slice(1);if(!['index.html','app.js','core.js','style.css','assets/icon-192.png','assets/icon-512.png','assets/poster.png'].includes(relative)){res.writeHead(404).end();return;}const file=path.resolve(root,relative);const data=await readFile(file);res.writeHead(200,{'Content-Type':{'.html':'text/html; charset=utf-8','.js':'text/javascript; charset=utf-8','.css':'text/css; charset=utf-8','.png':'image/png'}[path.extname(file)],'Cache-Control':'no-store'}).end(data);}catch{res.writeHead(404).end('Not found');}}).listen(5173,'127.0.0.1',()=>console.log('niu http://127.0.0.1:5173'));
