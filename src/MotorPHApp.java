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

        BufferedReader reader = openFile(employeeDataFile);
        String line;
        boolean found = false;

        while ((line = reader.readLine()) != null) {
            String[] employeeData = parseCSVLine(line);
            if (employeeData[0].trim().equals(employeeNumber)) {
                printEmployeeDetails(employeeData, null);
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
                    BufferedReader reader = openFile(employeeDataFile);
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
    // Prints the employee's payroll breakdown for June through December by computing
    // and displaying the payroll data for each month.
    public static void displayPayroll(String[] employeeData) throws Exception {
        String employeeNumber = employeeData[0];
        double hourlyRate = parseMoney(employeeData[18]);
        int year = getYearFromAttendance(employeeNumber);

        printEmployeeDetails(employeeData, hourlyRate);

        for (int month = 6; month <= 12; month++) {
            int lastDay = YearMonth.of(year, month).lengthOfMonth();
            double[] payrollData = computePayroll(employeeNumber, month, lastDay, hourlyRate);
            printPayroll(month, lastDay, payrollData);
        }
    }

    // Computes and returns the payroll figures for a given month as an array containing
    // hours and gross for both periods, plus SSS, PhilHealth, Pag-IBIG, tax, and total deductions.
    public static double[] computePayroll(String employeeNumber, int month, int lastDay, double hourlyRate) throws Exception {
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

        double[] payrollData = { hours1st, gross1st, hours2nd, gross2nd, sss, philHealth, pagIbig, tax, totalDeductions };
        return payrollData;
    }

    // Prints the hours worked, gross salary, deductions, and net salary for both
    // payroll periods of the given month using the precomputed payroll data.
    public static void printPayroll(int month, int lastDay, double[] payrollData) {
        System.out.println("\n--- " + MONTH_NAMES[month] + " 1 to 15 ---");
        System.out.printf("Total Hours Worked: %.2f%n", payrollData[0]);
        System.out.printf("Gross Salary: ₱%,.2f%n", payrollData[1]);
        System.out.printf("Net Salary: ₱%,.2f%n", payrollData[1]);

        System.out.println("\n--- " + MONTH_NAMES[month] + " 16 to " + lastDay + " ---");
        System.out.printf("Total Hours Worked: %.2f%n", payrollData[2]);
        System.out.printf("Gross Salary: ₱%,.2f%n", payrollData[3]);
        System.out.println("Deductions:");
        System.out.printf("  SSS: ₱%,.2f%n", payrollData[4]);
        System.out.printf("  PhilHealth: ₱%,.2f%n", payrollData[5]);
        System.out.printf("  Pag-IBIG: ₱%,.2f%n", payrollData[6]);
        System.out.printf("  Tax: ₱%,.2f%n", payrollData[7]);
        System.out.printf("Total Deductions: ₱%,.2f%n", payrollData[8]);
        System.out.printf("Net Salary: ₱%,.2f%n", payrollData[3] - payrollData[8]);
    }

    // ==================== HOURS CALCULATION ====================

    // Calculates total hours worked by an employee within a given date range in a month
    // by reading and filtering the attendance CSV records.
    public static double computeHoursForPeriod(String employeeNumber, int monthInput, int startDay, int endDay) throws Exception {
        double totalHours = 0;
        BufferedReader reader = openFile(attendanceRecords);
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

    // Returns the employee's SSS contribution by looking up the gross salary
    // against a tiered bracket table stored in a 2D array.
    public static double computeSSS(double grossSalary) {
        double[][] sssBrackets = {
                { 3250, 135.00 },  { 3750, 157.50 },  { 4250, 180.00 },  { 4750, 202.50 },
                { 5250, 225.00 },  { 5750, 247.50 },  { 6250, 270.00 },  { 6750, 292.50 },
                { 7250, 315.00 },  { 7750, 337.50 },  { 8250, 360.00 },  { 8750, 382.50 },
                { 9250, 405.00 },  { 9750, 427.50 },  { 10250, 450.00 }, { 10750, 472.50 },
                { 11250, 495.00 }, { 11750, 517.50 }, { 12250, 540.00 }, { 12750, 562.50 },
                { 13250, 585.00 }, { 13750, 607.50 }, { 14250, 630.00 }, { 14750, 652.50 },
                { 15250, 675.00 }, { 15750, 697.50 }, { 16250, 720.00 }, { 16750, 742.50 },
                { 17250, 765.00 }, { 17750, 787.50 }, { 18250, 810.00 }, { 18750, 832.50 },
                { 19250, 855.00 }, { 19750, 877.50 }, { 20250, 900.00 }, { 20750, 922.50 },
                { 21250, 945.00 }, { 21750, 967.50 }, { 22250, 990.00 }, { 22750, 1012.50 },
                { 23250, 1035.00 }, { 23750, 1057.50 }, { 24250, 1080.00 }, { 24750, 1102.50 }
        };

        // Scan and return the contribution for the first matching bracket.
        for (double[] bracket : sssBrackets) {
            if (grossSalary < bracket[0]) return bracket[1];
        }

        return 1125.00; // 24,750 and over
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
        double tax;

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

    // Opens a CSV file and skips the header line, returning a ready-to-read BufferedReader.
    public static BufferedReader openFile(String filePath) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        reader.readLine(); // Skip header line
        return reader;
    }

    // Parses a CSV line into a string array, treating commas inside
    // double quotes as part of the field rather than a separator.
    public static String[] parseCSVLine(String line) {
        // Count total fields by tracking commas outside quoted sections.
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

        // Extract each field, splitting only on unquoted commas.
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
        // Save the last field since it has no trailing comma.
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
        BufferedReader reader = openFile(employeeDataFile);
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
        BufferedReader reader = openFile(attendanceRecords);
        String line;
        while ((line = reader.readLine()) != null) {
            String[] attendanceData = parseCSVLine(line);
            if (attendanceData[0].trim().equals(employeeNumber)) {
                reader.close();
                return Integer.parseInt(attendanceData[3].trim().split("/")[2]);
            }
        }
        reader.close();
        return java.time.Year.now().getValue(); // Fallback to current year if no record is found.
    }

    // Prints employee details. Pass hourlyRate to include it, or null to ignore.
    public static void printEmployeeDetails(String[] employeeData, Double hourlyRate) {
        System.out.println("\n===============================");
        System.out.println("Employee #: " + employeeData[0]);
        System.out.println("Employee Name: " + employeeData[1] + ", " + employeeData[2]);
        System.out.println("Birthday: " + employeeData[3]);
        if (hourlyRate != null) {
            System.out.printf("Hourly Rate: ₱%,.2f%n", hourlyRate);
        }
        System.out.println("===============================");
    }
}