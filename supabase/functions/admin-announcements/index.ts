import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, apikey, content-type, x-announcement-password",
};
const announcementTimeZone = "Europe/London";
const fallbackAnnouncementAdminPassword = "A4ZHkdpZij18B7W1";

type AnnouncementRequestBody = Record<string, unknown>;

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

async function deleteAnnouncementById(
  supabase: ReturnType<typeof createClient>,
  announcementId: string,
) {
  const { data: row, error: fetchError } = await supabase
    .from("announcements")
    .select("id,media_path")
    .eq("id", announcementId)
    .maybeSingle();

  if (fetchError) {
    throw new Error(fetchError.message);
  }
  if (!row) {
    throw new Error("Announcement not found.");
  }

  if (row.media_path) {
    const { error: storageDeleteError } = await supabase.storage
      .from("announcement-media")
      .remove([row.media_path]);

    if (storageDeleteError) {
      throw new Error(storageDeleteError.message);
    }
  }

  const { error: deleteError } = await supabase
    .from("announcements")
    .delete()
    .eq("id", announcementId);

  if (deleteError) {
    throw new Error(deleteError.message);
  }
}

function parseLondonDateTimeAsUtc(dateText: string, timeText: string) {
  const dateMatch = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(dateText);
  const timeMatch = /^(\d{2}):(\d{2})$/.exec(timeText);
  if (!dateMatch || !timeMatch) {
    return null;
  }

  const [, dayText, monthText, yearText] = dateMatch;
  const [, hourText, minuteText] = timeMatch;
  const day = Number(dayText);
  const month = Number(monthText);
  const year = Number(yearText);
  const hour = Number(hourText);
  const minute = Number(minuteText);
  let candidate = new Date(Date.UTC(year, month - 1, day, hour, minute));

  // Convert the London wall-clock time picked in the app into its real UTC instant.
  const formatter = new Intl.DateTimeFormat("en-GB", {
    timeZone: announcementTimeZone,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  });
  const actualParts = Object.fromEntries(
    formatter
      .formatToParts(candidate)
      .filter((part) => part.type !== "literal")
      .map((part) => [part.type, part.value]),
  );
  const actualAsUtc = Date.UTC(
    Number(actualParts.year),
    Number(actualParts.month) - 1,
    Number(actualParts.day),
    Number(actualParts.hour),
    Number(actualParts.minute),
  );
  const desiredAsUtc = Date.UTC(year, month - 1, day, hour, minute);
  candidate = new Date(candidate.getTime() + (desiredAsUtc - actualAsUtc));

  const verifiedParts = Object.fromEntries(
    formatter
      .formatToParts(candidate)
      .filter((part) => part.type !== "literal")
      .map((part) => [part.type, part.value]),
  );
  const matchesLocalTime =
    Number(verifiedParts.year) === year &&
    Number(verifiedParts.month) === month &&
    Number(verifiedParts.day) === day &&
    Number(verifiedParts.hour) === hour &&
    Number(verifiedParts.minute) === minute;

  return matchesLocalTime ? candidate : null;
}

async function readRequestBody(request: Request): Promise<AnnouncementRequestBody> {
  const rawBody = await request.text();
  if (!rawBody.trim()) {
    return {};
  }

  const parsed = JSON.parse(rawBody);
  if (typeof parsed === "string") {
    const nested = JSON.parse(parsed);
    return isRecord(nested) ? nested : {};
  }

  return isRecord(parsed) ? parsed : {};
}

function isRecord(value: unknown): value is AnnouncementRequestBody {
  return typeof value === "object" && value !== null;
}

function readBodyString(body: AnnouncementRequestBody, ...keys: string[]) {
  for (const key of keys) {
    const value = body[key];
    if (typeof value === "string") {
      return value.trim();
    }
  }
  return "";
}

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  const expectedPassword = Deno.env.get("ANNOUNCEMENT_ADMIN_PASSWORD") ?? fallbackAnnouncementAdminPassword;
  const suppliedPassword = request.headers.get("x-announcement-password");
  if (!expectedPassword || suppliedPassword !== expectedPassword) {
    return new Response(JSON.stringify({ error: "Invalid announcement password." }), {
      status: 401,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
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
  const body = await readRequestBody(request);

  if (request.method === "DELETE") {
    const announcementId = readBodyString(body, "announcementId", "announcement_id", "id");
    if (!announcementId) {
      return new Response(JSON.stringify({ error: "Announcement ID is required." }), {
        status: 400,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    try {
      await deleteAnnouncementById(supabase, announcementId);
    } catch (error) {
      const message = error instanceof Error ? error.message : "Could not delete announcement.";
      return new Response(JSON.stringify({ error: message }), {
        status: message == "Announcement not found." ? 404 : 400,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    return new Response(JSON.stringify({ success: true }), {
      status: 200,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }

  const title = readBodyString(body, "title");
  const description = readBodyString(body, "description");
  const startDate = readBodyString(body, "startDate", "start_date");
  const startTime = readBodyString(body, "startTime", "start_time");
  const eventDate = readBodyString(body, "eventDate", "event_date", "expiryDate", "expiry_date");
  const eventTime = readBodyString(body, "eventTime", "event_time", "expiryTime", "expiry_time");
  const mediaBase64 = readBodyString(body, "mediaBase64", "media_base64");
  const mediaMimeType = readBodyString(body, "mediaMimeType", "media_mime_type");
  const mediaFileName = readBodyString(body, "mediaFileName", "media_file_name");

  if (!title || !description || !startDate || !startTime || !eventDate || !eventTime) {
    return new Response(JSON.stringify({ error: "Title, description, start date/time, and expiry date/time are required." }), {
      status: 400,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }

  let mediaUrl: string | null = null;
  let mediaPath: string | null = null;
  const startsAt = parseLondonDateTimeAsUtc(startDate, startTime);
  const expiresAt = parseLondonDateTimeAsUtc(eventDate, eventTime);

  if (!startsAt || !expiresAt || Number.isNaN(startsAt.getTime()) || Number.isNaN(expiresAt.getTime())) {
    return new Response(JSON.stringify({ error: "Start and expiry must use DD/MM/YYYY dates and HH:MM times." }), {
      status: 400,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }
  if (expiresAt.getTime() <= startsAt.getTime()) {
    return new Response(JSON.stringify({ error: "Expiry time must be after the start time." }), {
      status: 400,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }

  if (mediaBase64) {
    const cleanBase64 = mediaBase64.replace(/\s+/g, "");
    const binary = Uint8Array.from(atob(cleanBase64), (char) => char.charCodeAt(0));
    const extension = mediaFileName.includes(".")
      ? mediaFileName.split(".").pop()
      : mediaMimeType.split("/").pop() || "jpg";
    const storagePath = `announcements/${crypto.randomUUID()}.${extension}`;
    const { error: uploadError } = await supabase.storage
      .from("announcement-media")
      .upload(storagePath, binary, {
        contentType: mediaMimeType || "image/jpeg",
        upsert: false,
      });

    if (uploadError) {
      return new Response(JSON.stringify({ error: uploadError.message }), {
        status: 400,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const { data: publicUrlData } = supabase.storage
      .from("announcement-media")
      .getPublicUrl(storagePath);
    mediaUrl = publicUrlData.publicUrl;
    mediaPath = storagePath;
  }

  const { error } = await supabase.from("announcements").insert({
    title,
    description,
    media_url: mediaUrl,
    media_path: mediaPath,
    start_date: startDate,
    start_time: startTime,
    start_at: startsAt.toISOString(),
    event_date: eventDate,
    event_time: eventTime,
    expires_at: expiresAt.toISOString(),
  });

  if (error) {
    return new Response(JSON.stringify({ error: error.message }), {
      status: 400,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }

  return new Response(JSON.stringify({ success: true }), {
    status: 200,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
});
