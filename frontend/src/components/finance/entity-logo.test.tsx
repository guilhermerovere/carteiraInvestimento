import { fireEvent,render,screen } from "@testing-library/react";
import { describe,expect,it,vi } from "vitest";
import { EntityLogo,logoUrl } from "./entity-logo";
describe("EntityLogo",()=>{
 it("aceita somente hosts Brapi confiáveis",()=>{expect(logoUrl("BRAPI","https://icons.brapi.dev/icons/BBAS3.svg")).toContain("icons.brapi.dev");expect(logoUrl("BRAPI","https://evil.example/a.svg")).toBeNull()});
 it("usa publishable key no CDN Logo.dev e troca imagem quebrada por fallback",()=>{vi.stubEnv("NEXT_PUBLIC_LOGO_DEV_PUBLISHABLE_KEY","pk_demo");expect(logoUrl("LOGO_DEV","AAPL")).toContain("img.logo.dev/ticker/AAPL");const {container}=render(<EntityLogo provider="BRAPI" reference="https://icons.brapi.dev/icons/BBAS3.svg" label="BBAS3"/>);fireEvent.error(container.querySelector("img")!);expect(screen.getByLabelText("Logo de BBAS3")).toHaveTextContent("BB")});
 it("fallback é determinístico e acessível",()=>{render(<EntityLogo provider={null} reference={null} label="XP Investimentos"/>);expect(screen.getByLabelText("Logo de XP Investimentos")).toHaveTextContent("XP")});
});
