import type { HTMLAttributes, TableHTMLAttributes } from "react";
import { cn } from "@/lib/utils";

export function Table({ className, ...props }: TableHTMLAttributes<HTMLTableElement>) { return <div className="ui-table-wrap"><table className={cn("ui-table", className)} {...props} /></div>; }
export function TableHeader(props: HTMLAttributes<HTMLTableSectionElement>) { return <thead {...props} />; }
export function TableBody(props: HTMLAttributes<HTMLTableSectionElement>) { return <tbody {...props} />; }
export function TableRow(props: HTMLAttributes<HTMLTableRowElement>) { return <tr {...props} />; }
export function TableHead(props: HTMLAttributes<HTMLTableCellElement>) { return <th scope="col" {...props} />; }
export function TableCell(props: HTMLAttributes<HTMLTableCellElement>) { return <td {...props} />; }
