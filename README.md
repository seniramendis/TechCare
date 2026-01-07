📱 TechCare - Device Repair & Service App
TechCare is a native Android application designed to streamline the process of booking and tracking technical repairs for smartphones, laptops, and home appliances. Built using Java and Android Studio, it features a robust local database and a user-friendly interface for managing service requests.

🚀 Key Features
🛠 Service Booking System: Users can browse service categories (Smartphone, Laptop, Appliances), describe issues, and schedule repair appointments.

📊 Live Repair Tracker: A real-time status simulation engine that updates the repair progress (Received → Diagnosing → Repairing → Testing → Completed) and calculates ETA.

💾 Local Database Management: Built with SQLite to handle:

User Authentication (Login/Signup/Profile).

Booking History & Status.

Saved Devices for quick booking.

Reviews and Ratings.

🔔 Smart Notifications: Local notifications trigger automatically when a repair status changes to "Ready for Pickup."

💡 Tech Tips Hub: An educational section providing users with device maintenance advice (e.g., Battery care, Water damage tips).

🔍 Global Search: A powerful search bar allowing users to find specific services, track Booking IDs, or navigate to app sections instantly.

🛠 Tech Stack
Language: Java

Frontend: XML (Material Design Components, Custom Drawables)

Database: SQLite (Custom DatabaseHelper implementation)

Architecture: MVC Pattern

Libraries:

Glide: For efficient image loading and caching.

ViewPager2: For interactive sliders and carousels.

📸 App Workflow
User Login/Signup: Secure authentication with local storage.

Home Dashboard: View active repairs, promo banners, and popular services.

Booking: Select a device, describe the fault, and pick a date.

Tracking: Watch the progress bar update in real-time as the "technician" works on the device.
