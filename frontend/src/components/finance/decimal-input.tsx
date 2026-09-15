"use client";
import { useId, useState, type ClipboardEvent, type InputHTMLAttributes } from "react";
import { editablePtBr, parsePtBrDecimal, parsePtBrDecimalPaste } from "@/lib/finance/decimal";
import { formatCurrencyInput, formatDecimal } from "@/lib/finance/format";

type Props={label:string;value:string;onValueChange:(display:string,canonical:string)=>void;scale:number;precision?:number;kind?:"money"|"decimal";currency?:"BRL"|"USD";help?:string;error?:string}&Omit<InputHTMLAttributes<HTMLInputElement>,"value"|"onChange"|"onPaste">;
export function DecimalInput({label,value,onValueChange,scale,precision=18,kind="decimal",currency,help,error,...props}:Props){
 const id=useId(),desc=id+"-desc",[localError,setLocalError]=useState("");
 const apply=(display:string,pasted=false)=>{if(scale===0&&!/^\d*$/.test(display)){setLocalError("Informe uma quantidade inteira positiva.");return;}const parsed=(pasted?parsePtBrDecimalPaste:parsePtBrDecimal)(display,scale,precision);if(parsed.error){setLocalError(scale===0?"Informe uma quantidade inteira positiva.":parsed.error);onValueChange(display,"");return;}if(scale===0&&parsed.canonical==="0"){setLocalError("Informe uma quantidade inteira positiva.");onValueChange(display,"");return;}setLocalError("");onValueChange(display,parsed.canonical??"");};
 const paste=(event:ClipboardEvent<HTMLInputElement>)=>{event.preventDefault();apply(event.clipboardData.getData("text"),true);};
 const visibleError=error??localError;
 const numericOnly=scale===0;
 return <label className="operation-field" htmlFor={id}><span>{label}</span><span className={currency?"currency-input":""}>{currency&&<span className="currency-input__prefix" aria-hidden="true">{currency==="BRL"?"R$":"US$"}</span>}<input {...props} type="text" id={id} value={value} inputMode={numericOnly?"numeric":"decimal"} aria-invalid={!!visibleError} aria-describedby={(help||visibleError)?desc:undefined} onPaste={paste} onFocus={()=>{const parsed=parsePtBrDecimalPaste(value,scale,precision);if(parsed.canonical) onValueChange(editablePtBr(parsed.canonical),parsed.canonical);}} onBlur={()=>{const parsed=parsePtBrDecimal(value,scale,precision);if(parsed.canonical) onValueChange(kind==="money"||currency?formatCurrencyInput(parsed.canonical):formatDecimal(parsed.canonical,0),parsed.canonical);}} onChange={event=>apply(event.target.value)}/></span>{(visibleError||help)&&<small id={desc} role={visibleError?"alert":undefined}>{visibleError??help}</small>}</label>;
}
export function MoneyInput(props:Omit<Props,"scale"|"kind">){return <DecimalInput {...props} scale={2} kind="money"/>;}
export function QuantityInput(props:Omit<Props,"scale"|"kind">){return <DecimalInput {...props} scale={0}/>;}
export function PriceInput(props:Omit<Props,"scale"|"kind">){return <DecimalInput {...props} scale={8}/>;}
