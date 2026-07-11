package com.synapse.helpdesk.seeder;

import com.synapse.helpdesk.models.Ticket;
import com.synapse.helpdesk.models.User;
import com.synapse.helpdesk.repositories.TicketRepository;
import com.synapse.helpdesk.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    public DatabaseSeeder(UserRepository userRepository,
                          TicketRepository ticketRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (userRepository.count() > 0) {
            return;
        }

        System.out.println("🌱 Seeding database...");

        List<User> agents = new ArrayList<>();
        List<User> employees = new ArrayList<>();

        // ===========================
        // CREATE AGENTS
        // ===========================

        String[] agentNames = {
                "Vikram Singh",
                "Pooja Desai",
                "Arjun Nair",
                "Ritika Kapoor",
                "Sanjay Joshi"
        };

        for (String name : agentNames) {
            String email = name.toLowerCase().replace(" ", ".") + "@synapse.in";
            agents.add(createUser(name, email, "password123", "ROLE_AGENT"));
        }

        // ===========================
        // CREATE EMPLOYEES
        // ===========================

        String[] employeeNames = {
                "Rahul Sharma",
                "Sneha Iyer",
                "Amit Patel",
                "Neha Verma",
                "Rohan Gupta",
                "Priya Mehta",
                "Aditya Jain",
                "Karan Malhotra",
                "Divya Shah",
                "Ankit Mishra",
                "Nikhil Rao",
                "Shreya Kulkarni",
                "Harsh Agarwal",
                "Mehul Shah",
                "Ananya Bose",
                "Yash Tiwari",
                "Deepak Kumar",
                "Nisha Reddy",
                "Ritesh Singh",
                "Akash Chouhan"
        };

        for (String name : employeeNames) {
            String email = name.toLowerCase().replace(" ", ".") + "@synapse.in";
            employees.add(createUser(name, email, "password123", "ROLE_USER"));
        }

        // ===========================
        // TICKET TITLES
        // ===========================

        String[][] ticketTemplates = {

                {"VPN not connecting","Unable to connect to office VPN after latest update."},
                {"Laptop running slow","System freezes frequently while using IntelliJ IDEA."},
                {"Email not syncing","Outlook emails are not syncing since morning."},
                {"Need software installation","Please install Docker Desktop on my laptop."},
                {"Unable to login","Getting Invalid Credentials although password is correct."},
                {"Printer offline","Printer near HR cabin is offline."},
                {"WiFi disconnecting","Office WiFi disconnects every few minutes."},
                {"Blue screen error","Laptop showing BSOD during startup."},
                {"Keyboard issue","Laptop keyboard automatically types random characters."},
                {"Mouse not working","Wireless mouse suddenly stopped working."},
                {"Need dual monitor","Need additional monitor for development work."},
                {"IntelliJ license expired","Please renew IntelliJ Ultimate License."},
                {"VS Code extensions missing","Extensions disappeared after system update."},
                {"Payroll portal error","Unable to download salary slip."},
                {"Attendance mismatch","Yesterday attendance marked absent."},
                {"Biometric not working","Finger scanner is not detecting."},
                {"Git access denied","Permission denied while pushing to GitHub."},
                {"Database connection error","Spring Boot unable to connect to PostgreSQL."},
                {"Jenkins pipeline failed","Deployment pipeline keeps failing."},
                {"Build server down","CI server unavailable since morning."},
                {"Need RAM upgrade","Laptop slows down while running Docker."},
                {"Headset issue","Headset microphone not working in Teams."},
                {"Camera not detected","Laptop webcam missing during meeting."},
                {"Teams crashes","Microsoft Teams closes automatically."},
                {"Slack notifications missing","Desktop notifications are not coming."},
                {"Forgot password","Need password reset."},
                {"Monitor flickering","Display flickers continuously."},
                {"Excel crashing","Excel closes while opening large files."},
                {"Need admin rights","Need temporary administrator access."},
                {"Internet very slow","Internet speed is extremely slow today."}
        };

        String[] priorities = {
                "LOW",
                "MEDIUM",
                "HIGH",
                "CRITICAL"
        };

        String[] statuses = {
                "OPEN",
                "IN_PROGRESS",
                "RESOLVED"
        };

        // ===========================
        // CREATE 100 TICKETS
        // ===========================

        for (int i = 1; i <= 100; i++) {

            String[] template = ticketTemplates[random.nextInt(ticketTemplates.length)];

            String title = template[0];

            String description = template[1] +
                    "\n\nTicket #" + i +
                    "\nReported by employee for project deliverables.";

            String priority = priorities[random.nextInt(priorities.length)];

            String status = statuses[random.nextInt(statuses.length)];

            User employee = employees.get(random.nextInt(employees.size()));

            User agent = status.equals("OPEN")
                    ? null
                    : agents.get(random.nextInt(agents.size()));

            createTicket(
                    title,
                    description,
                    priority,
                    employee,
                    agent,
                    status
            );
        }

        System.out.println("====================================");
        System.out.println("✅ 5 Agents Created");
        System.out.println("✅ 20 Employees Created");
        System.out.println("✅ 100 Tickets Created");
        System.out.println("====================================");
    }

    private User createUser(String name,
                            String email,
                            String password,
                            String role) {

        User user = new User();

        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    private void createTicket(String title,
                              String description,
                              String priority,
                              User createdBy,
                              User assignedTo,
                              String status) {

        Ticket ticket = new Ticket();

        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setPriority(priority);

        ticket.setCreatedBy(createdBy);
        ticket.setAssignedTo(assignedTo);

        ticket.setStatus(status);

        LocalDateTime created =
                LocalDateTime.now().minusDays(random.nextInt(30))
                        .minusHours(random.nextInt(24))
                        .minusMinutes(random.nextInt(60));

        ticket.setCreatedAt(created);

        if (status.equals("RESOLVED")) {
            ticket.setUpdatedAt(
                    created.plusDays(random.nextInt(5) + 1)
            );
        } else {
            ticket.setUpdatedAt(created);
        }

        ticketRepository.save(ticket);
    }
}