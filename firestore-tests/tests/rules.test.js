// Security rules tests for ../firestore.rules, run with:
//   npm test   (which wraps `node --test` in `firebase emulators:exec`)
const { test, before, after, beforeEach } = require("node:test");
const {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails,
} = require("@firebase/rules-unit-testing");
const { readFileSync } = require("node:fs");
const { doc, getDoc, setDoc, updateDoc, deleteDoc } = require("firebase/firestore");

let env;

const ALICE = "alice-uid";
const BOB = "bob-uid";

const validUser = (uid) => ({ uid, displayName: "Alice", email: "alice@example.com" });
const validCorridor = (id) => ({
  corridorId: id,
  corridorName: "Koridor Test",
  polyline: "encoded-polyline",
  densityStatus: "LANCAR",
  currentSpeed: 32.5,
  lastUpdated: 1763164800000,
});
const validTelemetry = (id, uid) => ({
  telemetryId: id,
  userId: uid,
  corridorId: "koridor-1",
  latitude: -6.9,
  longitude: 107.6,
  speedKmh: 24.2,
});
const validReport = (id, uid) => ({
  reportId: id,
  userId: uid,
  corridorId: "koridor-1",
  reportedStatus: "MACET",
  latitude: -6.9,
  longitude: 107.6,
  representativeSpeed: 8.4,
});

before(async () => {
  env = await initializeTestEnvironment({
    projectId: "goout-rules-test",
    firestore: {
      rules: readFileSync("../firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8080,
    },
  });
});

after(async () => { await env.cleanup(); });
beforeEach(async () => { await env.clearFirestore(); });

const db = (uid) => (uid ? env.authenticatedContext(uid) : env.unauthenticatedContext()).firestore();

// ── users ──────────────────────────────────────────────────────
test("users: owner can create their own valid profile", async () => {
  await assertSucceeds(setDoc(doc(db(ALICE), "users", ALICE), validUser(ALICE)));
});

test("users: UID impersonation — writing another user's doc is denied", async () => {
  await assertFails(setDoc(doc(db(BOB), "users", ALICE), validUser(ALICE)));
});

test("users: embedded uid field must match the document id", async () => {
  await assertFails(setDoc(doc(db(ALICE), "users", ALICE), validUser(BOB)));
});

test("users: wrong field type (displayName as number) is denied", async () => {
  await assertFails(setDoc(doc(db(ALICE), "users", ALICE), { uid: ALICE, displayName: 42, email: "a@b.c" }));
});

test("users: unauthenticated (guest) write is denied", async () => {
  await assertFails(setDoc(doc(db(null), "users", ALICE), validUser(ALICE)));
});

test("users: reading another user's profile is denied", async () => {
  await assertFails(getDoc(doc(db(BOB), "users", ALICE)));
});

test("users: delete is always denied", async () => {
  await env.withSecurityRulesDisabled(async (c) =>
    setDoc(doc(c.firestore(), "users", ALICE), validUser(ALICE)));
  await assertFails(deleteDoc(doc(db(ALICE), "users", ALICE)));
});

// ── corridors ──────────────────────────────────────────────────
const seedCorridor = async (id) => {
  await env.withSecurityRulesDisabled(async (c) =>
    setDoc(doc(c.firestore(), "corridors", id), validCorridor(id)));
};

test("corridors: public (guest) read is allowed", async () => {
  await seedCorridor("koridor-1");
  await assertSucceeds(getDoc(doc(db(null), "corridors", "koridor-1")));
});

test("corridors: authenticated update of live fields only is allowed", async () => {
  await seedCorridor("koridor-1");
  await assertSucceeds(updateDoc(doc(db(ALICE), "corridors", "koridor-1"), {
    densityStatus: "MACET", currentSpeed: 7.1, lastUpdated: 1763165000000,
  }));
});

test("corridors: update touching protected field (corridorName) is denied", async () => {
  await seedCorridor("koridor-1");
  await assertFails(updateDoc(doc(db(ALICE), "corridors", "koridor-1"), {
    densityStatus: "MACET", currentSpeed: 7.1, lastUpdated: 1763165000000,
    corridorName: "Hacked",
  }));
});

test("corridors: invalid densityStatus value is denied", async () => {
  await seedCorridor("koridor-1");
  await assertFails(updateDoc(doc(db(ALICE), "corridors", "koridor-1"), {
    densityStatus: "GRIDLOCK", currentSpeed: 7.1, lastUpdated: 1763165000000,
  }));
});

test("corridors: out-of-range speed (>=200) is denied", async () => {
  await seedCorridor("koridor-1");
  await assertFails(updateDoc(doc(db(ALICE), "corridors", "koridor-1"), {
    densityStatus: "LANCAR", currentSpeed: 250, lastUpdated: 1763165000000,
  }));
});

test("corridors: unauthenticated (guest) update is denied", async () => {
  await seedCorridor("koridor-1");
  await assertFails(updateDoc(doc(db(null), "corridors", "koridor-1"), {
    densityStatus: "MACET", currentSpeed: 7.1, lastUpdated: 1763165000000,
  }));
});

test("corridors: delete is denied even when authenticated", async () => {
  await seedCorridor("koridor-1");
  await assertFails(deleteDoc(doc(db(ALICE), "corridors", "koridor-1")));
});

// ── telemetry ──────────────────────────────────────────────────
test("telemetry: user can create telemetry under their own uid", async () => {
  await assertSucceeds(setDoc(doc(db(ALICE), "telemetry", "t1"), validTelemetry("t1", ALICE)));
});

test("telemetry: UID impersonation (userId of another user) is denied", async () => {
  await assertFails(setDoc(doc(db(BOB), "telemetry", "t1"), validTelemetry("t1", ALICE)));
});

test("telemetry: wrong field type (speedKmh as string) is denied", async () => {
  const bad = { ...validTelemetry("t1", ALICE), speedKmh: "fast" };
  await assertFails(setDoc(doc(db(ALICE), "telemetry", "t1"), bad));
});

test("telemetry: negative speed is denied", async () => {
  const bad = { ...validTelemetry("t1", ALICE), speedKmh: -5 };
  await assertFails(setDoc(doc(db(ALICE), "telemetry", "t1"), bad));
});

test("telemetry: unauthenticated (guest) create is denied", async () => {
  await assertFails(setDoc(doc(db(null), "telemetry", "t1"), validTelemetry("t1", ALICE)));
});

test("telemetry: reads are denied even for the owner", async () => {
  await env.withSecurityRulesDisabled(async (c) =>
    setDoc(doc(c.firestore(), "telemetry", "t1"), validTelemetry("t1", ALICE)));
  await assertFails(getDoc(doc(db(ALICE), "telemetry", "t1")));
});

test("telemetry: idempotent overwrite of own doc is allowed", async () => {
  await env.withSecurityRulesDisabled(async (c) =>
    setDoc(doc(c.firestore(), "telemetry", "t1"), validTelemetry("t1", ALICE)));
  await assertSucceeds(setDoc(doc(db(ALICE), "telemetry", "t1"), validTelemetry("t1", ALICE)));
});

// ── reports ────────────────────────────────────────────────────
test("reports: user can create a report under their own uid", async () => {
  await assertSucceeds(setDoc(doc(db(ALICE), "reports", "r1"), validReport("r1", ALICE)));
});

test("reports: UID impersonation is denied", async () => {
  await assertFails(setDoc(doc(db(BOB), "reports", "r1"), validReport("r1", ALICE)));
});

test("reports: invalid reportedStatus is denied", async () => {
  const bad = { ...validReport("r1", ALICE), reportedStatus: "JAMMED" };
  await assertFails(setDoc(doc(db(ALICE), "reports", "r1"), bad));
});

test("reports: wrong field type (latitude as string) is denied", async () => {
  const bad = { ...validReport("r1", ALICE), latitude: "-6.9" };
  await assertFails(setDoc(doc(db(ALICE), "reports", "r1"), bad));
});

test("reports: unauthenticated (guest) create is denied", async () => {
  await assertFails(setDoc(doc(db(null), "reports", "r1"), validReport("r1", ALICE)));
});

test("reports: unauthenticated (guest) read is denied", async () => {
  await assertFails(getDoc(doc(db(null), "reports", "r1")));
});

// ── everything else ────────────────────────────────────────────
test("unknown collections: all access denied", async () => {
  await assertFails(setDoc(doc(db(ALICE), "admin", "x"), { a: 1 }));
  await assertFails(getDoc(doc(db(ALICE), "admin", "x")));
});
