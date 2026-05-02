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
  const normalizedDate = normalizeAnnouncementDate(dateText);
  const normalizedTime = normalizeAnnouncementTime(timeText);
  if (!normalizedDate || !normalizedTime) {
    return null;
  }

  const dateMatch = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(normalizedDate);
  const timeMatch = /^(\d{2}):(\d{2})$/.exec(normalizedTime);
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

function normalizeAnnouncementDate(value: string) {
  const trimmed = value.trim();
  const slashOrDashMatch = /^(\d{1,2})[/-](\d{1,2})[/-](\d{4})$/.exec(trimmed);
  const isoMatch = /^(\d{4})-(\d{1,2})-(\d{1,2})$/.exec(trimmed);
  const day = slashOrDashMatch ? Number(slashOrDashMatch[1]) : isoMatch ? Number(isoMatch[3]) : NaN;
  const month = slashOrDashMatch ? Number(slashOrDashMatch[2]) : isoMatch ? Number(isoMatch[2]) : NaN;
  const year = slashOrDashMatch ? Number(slashOrDashMatch[3]) : isoMatch ? Number(isoMatch[1]) : NaN;

  if (!isValidAnnouncementDate(day, month, year)) {
    return null;
  }

  return [
    String(day).padStart(2, "0"),
    String(month).padStart(2, "0"),
    String(year).padStart(4, "0"),
  ].join("/");
}

function normalizeAnnouncementTime(value: string) {
  const match = /^(\d{1,2}):(\d{2})$/.exec(value.trim());
  if (!match) {
    return null;
  }

  const hour = Number(match[1]);
  const minute = Number(match[2]);
  if (
    !Number.isInteger(hour) ||
    !Number.isInteger(minute) ||
    hour < 0 ||
    hour > 23 ||
    minute < 0 ||
    minute > 59
  ) {
    return null;
  }

  return `${String(hour).padStart(2, "0")}:${String(minute).padStart(2, "0")}`;
}

function isValidAnnouncementDate(day: number, month: number, year: number) {
  if (!Number.isInteger(day) || !Number.isInteger(month) || !Number.isInteger(year)) {
    return false;
  }
  if (year < 2000 || year > 2099 || month < 1 || month > 12) {
    return false;
  }

  const daysInMonth = [31, isLeapYear(year) ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][
    month - 1
  ];
  return day >= 1 && day <= daysInMonth;
}

function isLeapYear(year: number) {
  return year % 4 === 0 && (year % 100 !== 0 || year % 400 === 0);
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

  const normalizedStartDate = normalizeAnnouncementDate(startDate);
  const normalizedStartTime = normalizeAnnouncementTime(startTime);
  const normalizedEventDate = normalizeAnnouncementDate(eventDate);
  const normalizedEventTime = normalizeAnnouncementTime(eventTime);
  let mediaUrl: string | null = null;
  let mediaPath: string | null = null;
  const startsAt = parseLondonDateTimeAsUtc(startDate, startTime);
  const expiresAt = parseLondonDateTimeAsUtc(eventDate, eventTime);

  if (
    !normalizedStartDate ||
    !normalizedStartTime ||
    !normalizedEventDate ||
    !normalizedEventTime ||
    !startsAt ||
    !expiresAt ||
    Number.isNaN(startsAt.getTime()) ||
    Number.isNaN(expiresAt.getTime())
  ) {
    return new Response(JSON.stringify({ error: "Start and expiry must use valid dates and HH:MM times." }), {
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
    start_date: normalizedStartDate,
    start_time: normalizedStartTime,
    start_at: startsAt.toISOString(),
    event_date: normalizedEventDate,
    event_time: normalizedEventTime,
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
