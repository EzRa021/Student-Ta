# Lab Management System — User Manual

This manual explains how the Lab Management System works for the two primary user roles: TA (Teaching Assistant) and Student. It is written as a step-by-step guide from login through common workflows, and it includes concrete suggestions for screenshots you can add to your document.

How to use this file
- Read the role-specific sections (TA / Student) for user-facing workflows and UI guidance.
- Add the recommended screenshots (filenames are suggested) in the indicated places. Each screenshot suggestion includes a caption and alt text.
- Use the appendix for technical or admin notes if needed.

---

## Quick overview

The application uses standard web authentication (JWT + refresh tokens). Users log in, the backend issues tokens, and the frontend stores tokens to authorize API calls. The TA dashboard is optimized for triage and real-time conversation with students; the Student dashboard is optimized for creating and tracking help requests.

Both dashboards use real-time updates (WebSockets) for new requests and replies.

---

## TA Dashboard — Full Manual

### Purpose
The TA dashboard is the TA's workspace for receiving, claiming, replying to, prioritizing, and resolving student help requests. It shows a live-updating list of requests and provides tools for conversation and administrative updates (priority, resolve).

### Login
What happens:
- TA navigates to the login screen and submits credentials.
- Backend validates credentials and returns an access token (JWT) and a refresh token.
- Frontend stores tokens and opens the TA dashboard; real-time sockets are connected.

What to capture (screenshot):
- `ta_login.png` — Login page showing username/email, password, and Login button.
  - Caption: "TA Login screen — enter credentials to access the TA dashboard."
  - Alt text: "Login form with username and password fields and a login button."

Suggested text to include in report:
- "Enter your TA credentials and click Login. Successful login opens the TA dashboard and activates live updates via WebSocket. If your token expires the client uses the refresh token to request a new access token."

### Dashboard landing / Main layout
What happens:
- After login, the dashboard shows a sidebar (navigation) and main content area.
- Common sidebar items: All Requests, In Progress, Statistics, Profile/Settings.

What to capture:
- `ta_dashboard_overview.png` — Full TA dashboard layout with sidebar and main panel.
  - Caption: "TA dashboard — sidebar navigation and main content area."
  - Alt text: "Full TA dashboard layout showing sidebar and main list panel."

Suggested text:
- "The left sidebar provides navigation. The main panel shows lists or request details depending on selection. Use the sidebar to switch between views."

### All Requests view (default for triage)
What it does:
- Displays all requests with statuses (PENDING, IN_PROGRESS, RESOLVED).
- Ordering: PENDING items appear at the top; within each status the newest items are first (most recent first).
- Controls include search, status filter, sort selector, and the action buttons per row (Claim/Assign, Priority, View Details).

User actions:
- Scan the pending list for incoming requests.
- Click the row or "View" action to open details.
- Click "Claim" to assign the request to yourself (TA). You may also assign to others depending on privilege.
- Change priority using the priority control.

What to capture:
- `ta_all_requests.png` — All Requests table with a PENDING row highlighted and the Claim button visible.
  - Caption: "All Requests view — pending requests appear at the top; use Claim to take ownership."
  - Alt text: "Table listing requests with a pending request highlighted and a Claim button."

Suggested text:
- "All Requests lists everything currently in the system. Click Claim to take ownership of a request; that moves it into In Progress and opens the conversation interface."

### In Progress view
What it does:
- Shows requests currently assigned to any TA (or to the current TA depending on configuration) and provides the reply interface and Resolve action.

User actions:
- Open a request to read the full description and conversation.
- Use the reply box to post a message; replies are stored and sent to the student in real time.
- Click Resolve to mark the request resolved.

What to capture:
- `ta_in_progress.png` — In Progress list with one assigned request and reply box visible.
  - Caption: "In Progress — active requests and the reply area for ongoing conversations."
  - Alt text: "List of in-progress requests with a reply form shown for an active request."

Suggested text:
- "In Progress shows live conversations for tasks you or other TAs are working on. Use the reply box to communicate fixes or ask follow-up questions."

### Request detail and replies
What it does:
- Shows request metadata (title, description, attachments, created time, student summary) and the threaded reply list.
- Replies display author, timestamp, and message text.

User actions:
- Read student message and context before replying.
- Use the reply box to create a reply. You may attach files if supported.

What to capture:
- `ta_request_detail.png` — Request detail showing description and metadata.
  - Caption: "Request detail — view full description, attachments and metadata."
  - Alt text: "Full request details including title, description, and attachments."
- `ta_reply_thread.png` — Reply thread with a sample TA reply and Send button highlighted.
  - Caption: "Reply thread — send replies to students using the reply box."
  - Alt text: "Message thread showing replies and a message input box."

Suggested text:
- "Open a request to see the full context. Use the reply field to communicate. Replies are saved and broadcast to the student and other connected TAs."

### Update priority
What it does:
- TAs can set a numeric priority. Higher priority requests may surface earlier in lists.

User actions:
- Click the priority control, change the value, and confirm.

What to capture:
- `ta_priority_update.png` — Small crop of priority control and visible confirmation toast.
  - Caption: "Update priority — change numeric priority to reorder requests."
  - Alt text: "Priority control input and a confirmation message."

Suggested text:
- "Adjust priority to indicate urgency. The UI will re-sort lists according to priority and time."

### Resolve request
What it does:
- Marks a request RESOLVED and removes it from the active triage lists.

User actions:
- After finishing assistance, click Resolve. Confirm if prompted.

What to capture:
- `ta_resolve_confirmation.png` — Confirmation toast and request status updated to RESOLVED.
  - Caption: "Resolve request — mark as complete to remove from active lists."
  - Alt text: "UI showing a request status changed to resolved with a confirmation message."

Suggested text:
- "Resolving a request closes the conversation and archives the request. Students will see the updated status."

### Statistics and sidebar
What it does:
- Shows quick counts such as Pending, In Progress, Resolved. These counts may be computed locally or provided by the server depending on configuration.

What to capture:
- `ta_stats_sidebar.png` — Crop of sidebar showing statistic badges.
  - Caption: "Quick stats — pending and in-progress counts for TAs."
  - Alt text: "Sidebar statistics showing counts of pending and in-progress requests."

Suggested text:
- "Use the sidebar stats for a quick sense of workload. If counts appear incorrect, refresh or check the server-side stats endpoint (admin-only)."

### Real-time behavior and error handling
What it does:
- WebSocket connection pushes new requests and replies to connected TAs and students. If the socket disconnects, the client attempts reconnect and refreshes data on reconnect.

What to capture (optional):
- `ta_live_update_before.png` and `ta_live_update_after.png` — Before and after showing a request appearing.
  - Caption: "Real-time update — new request appears without a page refresh."
  - Alt text: "Request list before and after a new request appears."

Suggested text:
- "The dashboard updates in real time. If you see stale data, check the connection status and click refresh to force a reload."

### Security & role notes for TAs
- TA endpoints are protected by role-based authorization. The backend enforces role checks even if the frontend hides buttons.
- Do not attempt admin actions unless you have the ADMIN role; those actions are separate and restricted.

### Screenshot and formatting tips (TA)
- Use PNG format, 1280×720 or higher for clarity.
- Crop to show the relevant UI area; include column headers for table screenshots.
- Annotate the screenshot with a single colored box or arrow highlighting the control discussed.

---

## Student Dashboard — Full Manual

### Purpose
The Student dashboard allows students to create help requests, monitor their status, view TA replies, edit or delete unclaimed requests, and receive real-time updates when TAs reply or resolve requests.

### Login
What happens:
- Student logs in on the same login page as TAs and is redirected to the Student dashboard.

What to capture:
- `student_login.png` — If the login screen differs visually, capture it; otherwise reuse `ta_login.png`.
  - Caption: "Student Login — sign in to create and track help requests."
  - Alt text: "Login form with fields for username and password."

Suggested text:
- "Students authenticate using their assigned credentials. After login they land on the Student dashboard where they can raise new requests and monitor replies."

### Create new request
What it does:
- Student opens "New Request" or "Create" form, fills title and description, optionally adds attachments, and submits; the backend saves the request with `PENDING` status.

User actions:
- Click New Request, fill fields, click Submit.

What to capture:
- `student_new_request.png` — Create Request form populated with example text.
  - Caption: "Create a new help request — provide a concise title and a full description." 
  - Alt text: "Form for creating a new help request with title and description fields."

Suggested text:
- "Provide a clear title and include steps to reproduce or code snippets where appropriate. Submit to create a PENDING request which TAs will triage."

### My Requests / status tracking
What it does:
- Displays the student's own requests with status badges and quick actions (View, Edit, Delete) for requests that aren't assigned.

User actions:
- Click View to see detail; Edit or Delete if the request is still unclaimed.

What to capture:
- `student_my_requests.png` — My Requests list showing statuses and action buttons.
  - Caption: "My Requests — track the status of your help requests."
  - Alt text: "List of student's requests with status badges and action buttons."

Suggested text:
- "Students can edit or delete requests that haven't been claimed. Once a TA claims a request, the student can still view replies and add follow-ups if allowed."

### View request and read replies
What it does:
- Shows full request details and the conversation thread of replies from TAs.

User actions:
- Read replies and add comment if the app allows student-side replies. Monitor timestamps for progress.

What to capture:
- `student_request_detail.png` — Request detail with thread showing TA replies.
  - Caption: "Request detail — view messages from TAs and the status of the request."
  - Alt text: "Detailed request view including replies from TAs."

Suggested text:
- "Open a request to see the full conversation. The student will receive replies in real time and can respond if permitted."

### Edit or delete request
What it does:
- Students may edit the title/description or delete the request while it is PENDING and not yet assigned.

What to capture:
- `student_edit_delete.png` — Edit form and delete confirmation dialog.
  - Caption: "Edit or delete your request while it is unclaimed."
  - Alt text: "Edit form and delete confirmation for a student's request."

Suggested text:
- "If you realize you need to clarify the request, edit it before a TA claims it. If the problem was resolved yourself, delete the request to keep the queue tidy."

### Real-time updates
What it does:
- When a TA replies or resolves a request you created, the dashboard updates immediately.

What to capture (optional):
- `student_real_time.png` or short animated GIF — Reply appearing in the thread.
  - Caption: "Real-time replies appear without refreshing the page."
  - Alt text: "Conversation thread before and after a new reply appears."

Suggested text:
- "Students see TA replies in real time and will be notified of status changes. If real-time fails, refresh the page."

### Privacy and safety notes for students
- Do not post sensitive personal data in request details.
- Keep messages clear and focused on troubleshooting steps or coursework questions.

---

## General documentation and screenshot checklist

Place images inline in your report immediately after the paragraph that describes the feature. Use consistent filenames and a small caption text under each image.

Suggested checklist (copy into your report editor):
- [ ] `ta_login.png` — TA Login screen
- [ ] `ta_dashboard_overview.png` — TA dashboard full view
- [ ] `ta_all_requests.png` — All Requests table
- [ ] `ta_in_progress.png` — In Progress view with reply box
- [ ] `ta_request_detail.png` — Request detail view
- [ ] `ta_reply_thread.png` — Reply thread with Send button
- [ ] `ta_priority_update.png` — Priority control
- [ ] `ta_resolve_confirmation.png` — Resolve confirmation
- [ ] `ta_stats_sidebar.png` — Sidebar stats
- [ ] `student_login.png` — Student Login (if different)
- [ ] `student_new_request.png` — Create Request form
- [ ] `student_my_requests.png` — My Requests list
- [ ] `student_request_detail.png` — Student request detail
- [ ] `student_reply_view.png` — Reply thread from student's view
- [ ] `student_edit_delete.png` — Edit/delete confirmation

For each image provide:
- A short caption (1 line) and an alt text entry.
- Optional: a one-line instruction for how to create the screenshot (e.g., "Open TA dashboard, click All Requests, select the first PENDING row, press PrtSc.")

---

## Appendix: Notes for authors

- Sensitive data: redact student names/emails and tokens if you publish this report.
- Technical pointers: If you want to show API examples, include a `curl` example for the main endpoints (login, create request, get my requests). I can generate these snippets on request.
- If you'd like, I can produce a single merged PDF-ready markdown with images embedded (once you upload the screenshots), or a lighter HTML change log.

---

If you want, I can now:
- Add figure captions and alt text entries as separate metadata blocks for each screenshot.
- Generate `curl` examples for login, create request, claim request, reply, and resolve flows.
- Add an appendix with relevant backend file references (controllers & services) mapping to each UI feature.

Tell me which of those you'd like next and I will update the file or create a new appendix file.
