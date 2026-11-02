(async()=>{
 const q=s=>document.querySelector(s),click=s=>q(s).click();
 const tick=()=>new Promise(r=>setTimeout(r,100));
 const assert=(value,message)=>{if(!value)throw Error(message);};
 async function submit(values){for(const [name,value] of Object.entries(values)){const input=q(`[name="${name}"]`);assert(input,'missing '+name);input.value=value;}q('#form').requestSubmit();await tick();assert(!q('#form .form-error')?.textContent,'form error: '+q('#form .form-error')?.textContent);}
 try {
  click('[data-action=fund]');
  await submit({name:'验收示例基金',code:'000001',shareClass:'A',account:'CI虚构账户'});
  click('[data-action=transaction]');
  await submit({receipt:'ci-first',submittedAt:'2025-01-01T10:00',amount:'1200',fee:'0',status:'pending'});
  let db=JSON.parse(localStorage.getItem('fund-diary-v1-real'));
  const core=await import('./core.js');assert(core.summary(db).totalCost===0n,'pending changed cost');
  click(`[data-edit="${db.transactions[0].id}"]`);
  await submit({status:'confirmed',actualNavDate:'2025-01-02',actualConfirmDate:'2025-01-03',nav:'1.2',shares:'1000',changeReason:'收到测试回执'});
  click('#modal [data-close]');click('[data-action=transaction]');
  await submit({receipt:'ci-second',submittedAt:'2025-02-01T10:00',amount:'800',fee:'0',status:'confirmed',actualNavDate:'2025-02-02',actualConfirmDate:'2025-02-03',nav:'1',shares:'800'});
  click('#modal [data-close]');click('[data-action=nav]');
  await submit({date:'2025-02-04',value:'0.95',publishedAt:'2025-02-04T20:00'});
  assert(document.body.innerText.includes('1710.00'),'NAV not reflected');
  assert(document.body.innerText.includes('-290.00'),'profit mismatch');
  db=JSON.parse(localStorage.getItem('fund-diary-v1-real'));
  assert(core.fmt(core.summary(db).rows[0].shares)==='1800.00','shares mismatch');
  const restored=core.mergeBackup(core.empty(),JSON.parse(JSON.stringify(db)));
  assert(core.fmt(core.summary(restored).totalValue)==='1710.00','backup mismatch');
  const before=JSON.stringify(db);click('[data-page=simulator]');q('[name=amount]').value='200';q('[name=nav]').value='1';q('#sim-form').requestSubmit();await tick();assert(q('#sim-result').innerText.includes('2000.0000'),'simulator result');assert(localStorage.getItem('fund-diary-v1-real')===before,'simulation mutated data');
  click('[data-page=overview]');window.__qaResult='PASS';
 }catch(e){window.__qaResult='FAIL: '+e.message;}
})();
