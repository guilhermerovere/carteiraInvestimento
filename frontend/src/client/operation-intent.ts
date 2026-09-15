import type { FrozenOperationIntent, OperationKind, OperationPayload } from "@/lib/finance/contracts";
export const RECOVERY_KEY="valore.pending-operation.v1";
export function freezeIntent(kind:OperationKind,payload:OperationPayload):FrozenOperationIntent{return{version:1,kind,key:crypto.randomUUID(),payload:structuredClone(payload),createdAt:new Date().toISOString(),ambiguous:false};}
export function saveAmbiguous(intent:FrozenOperationIntent){sessionStorage.setItem(RECOVERY_KEY,JSON.stringify({...intent,ambiguous:true}));}
export function loadAmbiguous():FrozenOperationIntent|null{try{const value=JSON.parse(sessionStorage.getItem(RECOVERY_KEY)??"null") as FrozenOperationIntent|null;return value?.version===1&&value.ambiguous&&typeof value.key==="string"?value:null;}catch{return null;}}
export function clearAmbiguous(){sessionStorage.removeItem(RECOVERY_KEY);}
