import { createClient } from "npm:@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, apikey, content-type",
  "Access-Control-Allow-Methods": "GET, OPTIONS",
};

type AnnouncementRow = {
  id: string;
  title: string;
  description: string;
  media_url: string | null;
  start_date: string;
  start_time: string;
  event_date: string;
  event_time: string;
  expires_at: string;
  created_at: string;
};
type Database = {
  public: {
    Tables: {
      announcements: {
        Row: AnnouncementRow;
        Insert: Record<string, unknown>;
        Update: Record<string, unknown>;
        Relationships: [];
      };
    };
    Views: Record<string, never>;
    Functions: Record<string, never>;
    Enums: Record<string, never>;
    CompositeTypes: Record<string, never>;
  };
};

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  if (request.method !== "GET") {
    return jsonResponse({ error: "Method not allowed." }, 405);
  }

  const supabaseUrl = Deno.env.get("SUPABASE_URL");
  const anonKey = Deno.env.get("SUPABASE_ANON_KEY");
  if (!supabaseUrl || !anonKey) {
    return jsonResponse({ error: "Supabase function secrets are not configured." }, 500);
  }

  const nowIso = new Date().toISOString();
  const supabase = createClient<Database>(supabaseUrl, anonKey);
  const { data, error } = await supabase
    .from("announcements")
    .select("id,title,description,media_url,start_date,start_time,event_date,event_time,created_at")
    .gt("expires_at", nowIso)
    .order("created_at", { ascending: false });

  if (error) {
    return jsonResponse({ error: error.message }, 400);
  }

  return jsonResponse(data ?? [], 200);
});

function jsonResponse(body: unknown, status: number) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}
