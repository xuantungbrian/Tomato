export interface Route{
    route: string;
    method: string;
    validation: unknown[];
    action: unknown;
    protected: boolean;
}