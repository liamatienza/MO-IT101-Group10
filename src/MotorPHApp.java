import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Scanner;

public class MotorPHApp {
    public static Scanner scanner = new Scanner(System.in);
    public static String employeeDataFile = "MotorPH_Employee Data - Employee Details.csv";
    public static String attendanceRecords = "MotorPH_Employee Data - Attendance Record.csv";

    static final String[] MONTH_NAMES = {
            "", "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    // Entry point of the application. Prompts for credentials and routes to the
    // appropriate menu based on the username and password entered.
    public static void main(String[] args) throws Exception {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (Objects.equals(username, "employee") && Objects.equals(password, "12345")) {
            employeeMenu();
        } else if (Objects.equals(username, "payroll_staff") && Objects.equals(password, "12345")) {
            payrollMenu();
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    // ==================== EMPLOYEE MENU ====================

    // Displays the employee menu in a loop, allowing the user to search by employee
    // number or exit the program.
    public static void employeeMenu() throws Exception {
        while (true) {
            System.out.println("\n--- Employee Menu ---");
            System.out.println("1. Enter Employee Number");
            System.out.println("2. Exit the program");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    searchEmployee();
                    break;
                case "2":
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // Prompts for an employee number and prints the matching employee's details.
    // Displays an error if no match is found.
    public static void searchEmployee() throws Exception {
        System.out.print("\nEnter employee number: ");
        String employeeNumber = scanner.nextLine();

        BufferedReader reader = new BufferedReader(new FileReader(employeeDataFile));
        reader.readLine(); // Skip the First Line
        String line;
        boolean found = false;

        while ((line = reader.readLine()) != null) {
            String[] employeeData = parseCSVLine(line);
            if (employeeData[0].trim().equals(employeeNumber)) {
                System.out.println("\n===============================");
                System.out.println("Employee Number: " + employeeData[0]);
                System.out.println("Employee Name: " + employeeData[1] + ", " + employeeData[2]);
                System.out.println("Birthday: " + employeeData[3]);
                System.out.println("===============================");
                found = true;
                break;
            }
        }
        reader.close();

        if (!found) System.out.println("Employee number does not exist.");
    }

    // ==================== PAYROLL MENU ====================

    // Displays the payroll staff menu in a loop, allowing the user to process
    // payroll or exit the program.
    public static void payrollMenu() throws Exception {
        while (true) {
            System.out.println("\n--- Payroll Staff Menu ---");
            System.out.println("1. Process Payroll");
            System.out.println("2. Exit the program");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    processPayrollMenu();
                    break;
                case "2":
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // Presents a sub-menu to process payroll for a single employee or all employees.
    public static void processPayrollMenu() throws Exception {
        while (true) {
            System.out.println("\n--- Process Payroll ---");
            System.out.println("1. One employee");
            System.out.println("2. All employees");
            System.out.println("3. Back");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.print("Enter employee number: ");
                    String employeeNumber = scanner.nextLine().trim();
                    String[] employeeData = findEmployee(employeeNumber);
                    if (employeeData == null) {
                        System.out.println("Employee number does not exist.");
                    } else {
                        displayPayroll(employeeData);
                    }
                    break;
                case "2":
                    BufferedReader reader = new BufferedReader(new FileReader(employeeDataFile));
                    reader.readLine(); // Skip First Line
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] data = parseCSVLine(line);
                        displayPayroll(data);
                        System.out.println("==================================================");
                    }
                    reader.close();
                    break;
                case "3":
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // ==================== PAYROLL DISPLAY ====================

    // Prints a full payroll breakdown for the given employee across June to December,
    // split into two periods per month with deductions applied in the second half.
    public static void displayPayroll(String[] employeeData) throws Exception {
        String employeeNumber = employeeData[0];
        String employeeName = employeeData[1] + ", " + employeeData[2];
        String birthday = employeeData[3];
        double hourlyRate = parseMoney(employeeData[18]);
        int year = getYearFromAttendance(employeeNumber);

        System.out.println("\nEmployee #: " + employeeNumber);
        System.out.println("Employee Name: " + employeeName);
        System.out.println("Birthday: " + birthday);
        System.out.println("Hourly Rate: " + hourlyRate);

        for (int month = 6; month <= 12; month++) {
            int lastDay = YearMonth.of(year, month).lengthOfMonth();
            double hours1st = computeHoursForPeriod(employeeNumber, month, 1, 15);
            double gross1st = hours1st * hourlyRate;
            double hours2nd = computeHoursForPeriod(employeeNumber, month, 16, lastDay);
            double gross2nd = hours2nd * hourlyRate;
            double totalGross = gross1st + gross2nd;
            double sss = computeSSS(totalGross);
            double philHealth = computePhilHealth(totalGross);
            double pagIbig = computePagIbig(totalGross);
            double taxableIncome = totalGross - (sss + philHealth + pagIbig);
            double tax = computeWithholdingTax(taxableIncome);
            double totalDeductions = sss + philHealth + pagIbig + tax;

            System.out.println("\n--- " + MONTH_NAMES[month] + " 1 to 15 ---");
            System.out.printf("Total Hours Worked: %.2f%n", hours1st);
            System.out.printf("Gross Salary: ₱%,.2f%n", gross1st);
            System.out.printf("Net Salary: ₱%,.2f%n", gross1st);

            System.out.println("\n--- " + MONTH_NAMES[month] + " 16 to " + lastDay + " ---");
            System.out.printf("Total Hours Worked: %.2f%n", hours2nd);
            System.out.printf("Gross Salary: ₱%,.2f%n", gross2nd);
            System.out.println("Deductions:");
            System.out.printf("  SSS: ₱%,.2f%n", sss);
            System.out.printf("  PhilHealth: ₱%,.2f%n", philHealth);
            System.out.printf("  Pag-IBIG: ₱%,.2f%n", pagIbig);
            System.out.printf("  Tax: ₱%,.2f%n", tax);
            System.out.printf("Total Deductions: ₱%,.2f%n", totalDeductions);
            System.out.printf("Net Salary: ₱%,.2f%n", gross2nd - totalDeductions);
        }
    }

    // ==================== HOURS CALCULATION ====================

    // Calculates total hours worked by an employee within a given date range in a month
    // by reading and filtering the attendance CSV records.
    public static double computeHoursForPeriod(String employeeNumber, int monthInput, int startDay, int endDay) throws Exception {
        double totalHours = 0;
        BufferedReader reader = new BufferedReader(new FileReader(attendanceRecords));
        reader.readLine(); // Skip the First Line
        String line;

        while ((line = reader.readLine()) != null) {
            String[] attendanceData = parseCSVLine(line);
            if (!attendanceData[0].trim().equals(employeeNumber)) continue;

            String[] dateParts = attendanceData[3].trim().split("/");
            int month = Integer.parseInt(dateParts[0]);
            int day = Integer.parseInt(dateParts[1]);

            if (month == monthInput && day >= startDay && day <= endDay) {
                totalHours += computeDailyHours(attendanceData[4].trim(), attendanceData[5].trim());
            }
        }
        reader.close();
        return totalHours;
    }

    // Computes hours worked in a single day based on login and logout times, applying
    // an 8AM–5PM work window, a 10-minute grace period, and a 1-hour lunch deduction.
    public static double computeDailyHours(String logIn, String logOut) {
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("H:mm");
        LocalTime start = LocalTime.parse(logIn, timeFormat);
        LocalTime end = LocalTime.parse(logOut, timeFormat);
        LocalTime workStart = LocalTime.of(8, 0);
        LocalTime workEnd = LocalTime.of(17, 0);
        LocalTime gracePeriodEnd = LocalTime.of(8, 10); // 10-minute grace period

        // If login is early or within grace period (8:00–8:10), treat as 8:00.
        // If login is 8:11 or later, use actual login time.
        LocalTime effectiveStart;
        if (!start.isAfter(gracePeriodEnd)) {
            effectiveStart = workStart;
        } else {
            effectiveStart = start;
        }

        LocalTime effectiveEnd;
        if (end.isAfter(workEnd)) {
            effectiveEnd = workEnd;
        } else {
            effectiveEnd = end;
        }

        if (!effectiveEnd.isAfter(effectiveStart)) return 0;

        long minutes = Duration.between(effectiveStart, effectiveEnd).toMinutes();
        if (minutes > 60) minutes -= 60;
        else minutes = 0;

        return minutes / 60.0;
    }

    // ==================== DEDUCTIONS ====================

    // Returns the employee's SSS contribution based on a tiered salary bracket table.
    public static double computeSSS(double grossSalary) {
        if (grossSalary < 3250) {
            return 135.00;
        } else if (grossSalary >= 3250 && grossSalary <= 3750) {
            return 157.50;
        } else if (grossSalary >= 3750 && grossSalary <= 4250) {
            return 180.00;
        } else if (grossSalary >= 4250 && grossSalary <= 4750) {
            return 202.50;
        } else if (grossSalary >= 4750 && grossSalary <= 5250) {
            return 225.00;
        } else if (grossSalary >= 5250 && grossSalary <= 5750) {
            return 247.50;
        } else if (grossSalary >= 5750 && grossSalary <= 6250) {
            return 270.00;
        } else if (grossSalary >= 6250 && grossSalary <= 6750) {
            return 292.50;
        } else if (grossSalary >= 6750 && grossSalary <= 7250) {
            return 315.00;
        } else if (grossSalary >= 7250 && grossSalary <= 7750) {
            return 337.50;
        } else if (grossSalary >= 7750 && grossSalary <= 8250) {
            return 360.00;
        } else if (grossSalary >= 8250 && grossSalary <= 8750) {
            return 382.50;
        } else if (grossSalary >= 8750 && grossSalary <= 9250) {
            return 405.00;
        } else if (grossSalary >= 9250 && grossSalary <= 9750) {
            return 427.50;
        } else if (grossSalary >= 9750 && grossSalary <= 10250) {
            return 450.00;
        } else if (grossSalary >= 10250 && grossSalary <= 10750) {
            return 472.50;
        } else if (grossSalary >= 10750 && grossSalary <= 11250) {
            return 495.00;
        } else if (grossSalary >= 11250 && grossSalary <= 11750) {
            return 517.50;
        } else if (grossSalary >= 11750 && grossSalary <= 12250) {
            return 540.00;
        } else if (grossSalary >= 12250 && grossSalary <= 12750) {
            return 562.50;
        } else if (grossSalary >= 12750 && grossSalary <= 13250) {
            return 585.00;
        } else if (grossSalary >= 13250 && grossSalary <= 13750) {
            return 607.50;
        } else if (grossSalary >= 13750 && grossSalary <= 14250) {
            return 630.00;
        } else if (grossSalary >= 14250 && grossSalary <= 14750) {
            return 652.50;
        } else if (grossSalary >= 14750 && grossSalary <= 15250) {
            return 675.00;
        } else if (grossSalary >= 15250 && grossSalary <= 15750) {
            return 697.50;
        } else if (grossSalary >= 15750 && grossSalary <= 16250) {
            return 720.00;
        } else if (grossSalary >= 16250 && grossSalary <= 16750) {
            return 742.50;
        } else if (grossSalary >= 16750 && grossSalary <= 17250) {
            return 765.00;
        } else if (grossSalary >= 17250 && grossSalary <= 17750) {
            return 787.50;
        } else if (grossSalary >= 17750 && grossSalary <= 18250) {
            return 810.00;
        } else if (grossSalary >= 18250 && grossSalary <= 18750) {
            return 832.50;
        } else if (grossSalary >= 18750 && grossSalary <= 19250) {
            return 855.00;
        } else if (grossSalary >= 19250 && grossSalary <= 19750) {
            return 877.50;
        } else if (grossSalary >= 19750 && grossSalary <= 20250) {
            return 900.00;
        } else if (grossSalary >= 20250 && grossSalary <= 20750) {
            return 922.50;
        } else if (grossSalary >= 20750 && grossSalary <= 21250) {
            return 945.00;
        } else if (grossSalary >= 21250 && grossSalary <= 21750) {
            return 967.50;
        } else if (grossSalary >= 21750 && grossSalary <= 22250) {
            return 990.00;
        } else if (grossSalary >= 22250 && grossSalary <= 22750) {
            return 1012.50;
        } else if (grossSalary >= 22750 && grossSalary <= 23250) {
            return 1035.00;
        } else if (grossSalary >= 23250 && grossSalary <= 23750) {
            return 1057.50;
        } else if (grossSalary >= 23750 && grossSalary <= 24250) {
            return 1080.00;
        } else if (grossSalary >= 24250 && grossSalary <= 24750) {
            return 1102.50;
        } else {
            return 1125.00;
        }
    }

    // Returns the employee's PhilHealth contribution at 1.5% of gross salary,
    // with no contribution below ₱10,000 and a ₱900 cap at ₱60,000 and above.
    public static double computePhilHealth(double grossSalary) {
        if (grossSalary < 10000) {
            return 0.00;
        } else if (grossSalary <= 59999.99) {
            return grossSalary * 0.015; // employee share is 50% of 3%
        } else {
            return 900.00;
        }
    }

    // Returns the employee's Pag-IBIG contribution (1%–2% of gross salary),
    // capped at a maximum of ₱100 per month.
    public static double computePagIbig(double grossSalary) {
        double contribution;
        if (grossSalary < 1000) {
            contribution = 0;
        } else if (grossSalary >= 1000 && grossSalary <= 1500) {
            contribution = grossSalary * 0.01;
        } else {
            contribution = grossSalary * 0.02;
        }

        if (contribution > 100) {
            return 100;
        }

        return contribution;
    }

    // Returns the monthly withholding tax based on tax brackets
    // applied to the employee's taxable income.
    public static double computeWithholdingTax(double taxableIncome) {
        double tax = 0.0;

        if (taxableIncome <= 20832) {
            tax = 0.0;
        } else if (taxableIncome <= 33333) {
            tax = (taxableIncome - 20833) * 0.20;
        } else if (taxableIncome <= 66667) {
            tax = 2500 + (taxableIncome - 33333) * 0.25;
        } else if (taxableIncome <= 166667) {
            tax = 10833 + (taxableIncome - 66667) * 0.30;
        } else if (taxableIncome <= 666667) {
            tax = 40833.33 + (taxableIncome - 166667) * 0.32;
        } else {
            tax = 200833.33 + (taxableIncome - 666667) * 0.35;
        }

        return tax;
    }

    // ==================== UTILITY ====================

    // Parses a CSV line into a string array, treating commas inside
    // double quotes as part of the field rather than a separator.
    public static String[] parseCSVLine(String line) {
        int count = 1;
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                count++;
            }
        }

        String[] fields = new String[count];
        int fieldIndex = 0;
        inQuotes = false;
        String current = "";

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields[fieldIndex++] = current;
                current = "";
            } else {
                current += c;
            }
        }
        fields[fieldIndex] = current;
        return fields;
    }

    // Strips commas and quotes from a money-formatted string and returns it as a double.
    public static double parseMoney(String value) {
        return Double.parseDouble(value.trim().replace(",", "").replace("\"", ""));
    }

    // Searches the employee CSV for a matching employee number and returns their data,
    // or null if not found.
    public static String[] findEmployee(String employeeNumber) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(employeeDataFile));
        reader.readLine(); // Skip the first line
        String line;
        while ((line = reader.readLine()) != null) {
            String[] employeeData = parseCSVLine(line);
            if (employeeData[0].trim().equals(employeeNumber)) {
                reader.close();
                return employeeData;
            }
        }
        reader.close();
        return null;
    }

    // Reads the year from the first matching attendance record for the given employee.
    public static int getYearFromAttendance(String employeeNumber) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(attendanceRecords));
        reader.readLine(); // Skip first line
        String line;
        while ((line = reader.readLine()) != null) {
            String[] attendanceData = parseCSVLine(line);
            if (attendanceData[0].trim().equals(employeeNumber)) {
                reader.close();
                return Integer.parseInt(attendanceData[3].trim().split("/")[2]);
            }
        }
        reader.close();
        return java.time.Year.now().getValue(); // Fallback to Current Year if no exact year is found.
    }
}