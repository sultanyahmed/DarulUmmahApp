import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, apikey, content-type",
};

async function cleanupExpiredAnnouncements(supabase: ReturnType<typeof createClient>) {
  const nowIso = new Date().toISOString();
  const { data: expiredRows, error: fetchExpiredError } = await supabase
    .from("announcements")
    .select("id,media_path")
    .lte("expires_at", nowIso);

  if (fetchExpiredError) {
    throw new Error(fetchExpiredError.message);
  }

  const mediaPaths = (expiredRows ?? [])
    .map((row) => row.media_path)
    .filter((path): path is string => Boolean(path));

  if (mediaPaths.length > 0) {
    const { error: storageDeleteError } = await supabase.storage
      .from("announcement-media")
      .remove(mediaPaths);

    if (storageDeleteError) {
      throw new Error(storageDeleteError.message);
    }
  }

  const expiredIds = (expiredRows ?? []).map((row) => row.id);
  if (expiredIds.length > 0) {
    const { error: deleteRowsError } = await supabase
      .from("announcements")
      .delete()
      .in("id", expiredIds);

    if (deleteRowsError) {
      throw new Error(deleteRowsError.message);
    }
  }
}

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  const supabaseUrl = Deno.env.get("SUPABASE_URL");
  const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  if (!supabaseUrl || !serviceRoleKey) {
    return new Response(JSON.stringify({ error: "Supabase function secrets are not configured." }), {
      status: 500,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }

  const supabase = createClient(supabaseUrl, serviceRoleKey);
  await cleanupExpiredAnnouncements(supabase);
  const { data, error } = await supabase
    .from("announcements")
    .select("id,title,description,media_url,event_date,event_time,created_at")
    .gt("expires_at", new Date().toISOString())
    .order("created_at", { ascending: false });

  if (error) {
    return new Response(JSON.stringify({ error: error.message }), {
      status: 400,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }

  return new Response(JSON.stringify(data ?? []), {
    status: 200,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
});
