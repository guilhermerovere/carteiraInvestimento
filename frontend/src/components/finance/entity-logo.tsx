"use client";
/* eslint-disable @next/next/no-img-element -- Logo.dev's CDN URL is runtime metadata and the approved design uses direct img delivery. */
import { useState } from "react";
import type { LogoProvider } from "@/lib/finance/contracts";

function initials(label: string) { const words=label.trim().split(/\s+/); return (words[0].length<=2?words[0]:words.length>1?words.slice(0,2).map(w=>w[0]).join(""):label.slice(0,2)).toUpperCase(); }
export function logoUrl(provider: LogoProvider | null, reference: string | null): string | null {
  if (!provider || !reference) return null;
  if (provider === "BRAPI") { try { const url=new URL(reference); return url.protocol==="https:" && url.hostname==="icons.brapi.dev" ? url.href : null; } catch { return null; } }
  const ticker=reference.match(/^ticker\/([A-Z0-9.-]+)$/i)?.[1];
  if (!ticker && !/^[a-z0-9.-]+$/i.test(reference)) return null;
  const token=process.env.NEXT_PUBLIC_LOGO_DEV_PUBLISHABLE_KEY;
  const path=ticker?`ticker/${encodeURIComponent(ticker)}`:reference.includes(".")?encodeURIComponent(reference):`ticker/${encodeURIComponent(reference)}`;
  return token ? `https://img.logo.dev/${path}?token=${encodeURIComponent(token)}&size=64&format=png&fallback=404` : null;
}
export function EntityLogo({provider,reference,label,size="md"}:{provider:LogoProvider|null;reference:string|null;label:string;size?:"sm"|"md"}) {
  const [failed,setFailed]=useState(false); const src=logoUrl(provider,reference);
  return <span className={`entity-logo entity-logo--${size}`} aria-label={`Logo de ${label}`}>{src&&!failed?<img src={src} alt="" onError={()=>setFailed(true)} />:<span aria-hidden="true">{initials(label)}</span>}</span>;
}
