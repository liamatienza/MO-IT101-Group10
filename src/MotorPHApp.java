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
    public static String employeeData = "MotorPH_Employee Data - Employee Details.csv";
    public static String attendanceRecords = "MotorPH_Employee Data - Attendance Record.csv";

    static final String[] MONTH_NAMES = {
            "", "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    // This is the entry point of the application. It prompts the user to enter a username
    // and password, then compares the input against two hardcoded credential sets to determine
    // which menu to display. If the credentials match "employee", the employee menu is shown.
    // If they match "payroll_staff", the payroll menu is shown. Otherwise, access is denied
    // and an error message is displayed.
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

    // This method displays the employee menu and keeps it active in a loop until the user
    // chooses to exit. It presents two options: entering an employee number to search for
    // the employee, or exiting the program. The user's input is read and matched using a switch
    // statement, and any unrecognized input triggers an error message before looping again.
    public static void employeeMenu() throws Exception {
        while (true) {
            System.out.println("\n--- Employee Menu ---");
            System.out.println("1. Enter Employee Number");
            System.out.println("2. Exit the program");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();

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

    // This method asks the user to input an employee number, then searches the employee CSV
    // file line by line for a matching record. Each line is parsed using parseCSVLine() to
    // extract individual fields. If a match is found, the employee's number, full name, and
    // birthday are printed. If no match is found after reading all records,
    // the user is informed that the employee number does not exist.
    public static void searchEmployee() throws Exception {
        System.out.print("\nEnter employee number: ");
        String employeeNumber = scanner.nextLine();

        BufferedReader reader = new BufferedReader(new FileReader(employeeData));
        reader.readLine(); // Skip the First Line
        String line;
        boolean found = false;

        while ((line = reader.readLine()) != null) {
            String[] empData = parseCSVLine(line);
            if (empData[0].trim().equals(employeeNumber)) {
                System.out.println("\n===============================");
                System.out.println("Employee Number: " + empData[0]);
                System.out.println("Employee Name: " + empData[1] + ", " + empData[2]);
                System.out.println("Birthday: " + empData[3]);
                System.out.println("===============================");
                found = true;
                break;
            }
        }
        reader.close();

        if (!found) System.out.println("Employee number does not exist.");
    }

    // ==================== PAYROLL MENU ====================

    // This method displays the payroll staff menu and keeps it active in a loop until the
    // user chooses to exit. It presents two options: processing payroll or exiting the
    // program. The user's input is read and matched using a switch statement. Selecting
    // option 1 calls processPayrollMenu(), and any unrecognized input triggers an error
    // message before the loop repeats.
    public static void payrollMenu() throws Exception {
        while (true) {
            System.out.println("\n--- Payroll Staff Menu ---");
            System.out.println("1. Process Payroll");
            System.out.println("2. Exit the program");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();

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

    // This method presents a sub-menu that allows payroll staff to process payroll either
    // for a single employee or for all employees at once. For a single employee, it prompts
    // for an employee number, uses findEmployee() to locate their record, and then calls
    // displayPayroll() with the result. For all employees, it opens the employee CSV file,
    // reads each record line by line, and calls displayPayroll() on each one. Selecting
    // option 3 returns to the previous menu.
    public static void processPayrollMenu() throws Exception {
        while (true) {
            System.out.println("\n--- Process Payroll ---");
            System.out.println("1. One employee");
            System.out.println("2. All employees");
            System.out.println("3. Back");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Enter employee number: ");
                    String empNum = scanner.nextLine().trim();
                    String[] empData = findEmployee(empNum);
                    if (empData == null) {
                        System.out.println("Employee number does not exist.");
                    } else {
                        displayPayroll(empData);
                    }
                    break;
                case "2":
                    BufferedReader reader = new BufferedReader(new FileReader(employeeData));
                    reader.readLine(); // skip header
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

    // This method prints a complete payroll breakdown for the given employee, covering the
    // months of June through December. For each month, it divides the period into two:
    // days 1 to 15, and days 16 to the end of the month. Hours worked for each half
    // are calculated using computeHoursForPeriod() and multiplied by the hourly rate to get
    // the gross salary. Deductions: SSS, PhilHealth, Pag-IBIG, and withholding tax are
    // computed once per month using the employee's monthly salary and applied entirely in the
    // second half of the month. The net salary for the second half is derived by subtracting
    // all deductions from the second half's gross salary.
    public static void displayPayroll(String[] empData) throws Exception {
        String empNum = empData[0];
        String empName = empData[1] + ", " + empData[2];
        double monthlySalary = parseMoney(empData[13]);
        double hourlyRate = parseMoney(empData[18]);

        System.out.println("\nEmployee #: " + empNum);
        System.out.println("Employee Name: " + empName);
        System.out.println("Birthday: " + empData[3]);
        System.out.println("Hourly Rate: " + hourlyRate);

        for (int month = 6; month <= 12; month++) {
            int lastDay = YearMonth.of(2024, month).lengthOfMonth();
            double hours1 = computeHoursForPeriod(empNum, month, 1, 15);
            double gross1 = hours1 * hourlyRate;
            double hours2 = computeHoursForPeriod(empNum, month, 16, lastDay);
            double gross2 = hours2 * hourlyRate;
            double totalGross = gross1 + gross2;
            double sss = computeSSS(monthlySalary);
            double philHealth = computePhilHealth(monthlySalary);
            double pagIbig = computePagIbig(monthlySalary);
            double taxableIncome = totalGross - (sss + philHealth + pagIbig);
            double tax = computeWithholdingTax(taxableIncome);
            double totalDeductions = sss + philHealth + pagIbig + tax;

            System.out.println("\n--- " + MONTH_NAMES[month] + " 1 to 15 ---");
            System.out.printf("Total Hours Worked: %.2f%n", hours1);
            System.out.printf("Gross Salary: ₱%,.2f%n", gross1);
            System.out.printf("Net Salary: ₱%,.2f%n", gross1);

            System.out.println("\n--- " + MONTH_NAMES[month] + " 16 to " + lastDay + " ---");
            System.out.printf("Total Hours Worked: %.2f%n", hours2);
            System.out.printf("Gross Salary: ₱%,.2f%n", gross2);
            System.out.println("Deductions:");
            System.out.printf("  SSS: ₱%,.2f%n", sss);
            System.out.printf("  PhilHealth: ₱%,.2f%n", philHealth);
            System.out.printf("  Pag-IBIG: ₱%,.2f%n", pagIbig);
            System.out.printf("  Tax: ₱%,.2f%n", tax);
            System.out.printf("Total Deductions: ₱%,.2f%n", totalDeductions);
            System.out.printf("Net Salary: ₱%,.2f%n", gross2 - totalDeductions);
        }
    }

    // ==================== HOURS CALCULATION ====================

    // This method calculates the total hours worked by a specific employee within a defined
    // date range in a given month. It reads through the attendance CSV file and skips any
    // records that do not belong to the target employee. For each matching record, it parses
    // the date to verify it falls within the specified day range, then calls computeDailyHours()
    // with the login and logout times to get that day's hours worked. All daily values are
    // accumulated and returned as a total.
    public static double computeHoursForPeriod(String empNum, int month, int startDay, int endDay) throws Exception {
        double totalHours = 0;
        BufferedReader reader = new BufferedReader(new FileReader(attendanceRecords));
        reader.readLine(); // Skip the First Line
        String line;

        while ((line = reader.readLine()) != null) {
            String[] attData = parseCSVLine(line);
            if (!attData[0].trim().equals(empNum)) continue;

            String[] dateParts = attData[3].trim().split("/");
            int m = Integer.parseInt(dateParts[0]);
            int d = Integer.parseInt(dateParts[1]);

            if (m == month && d >= startDay && d <= endDay) {
                totalHours += computeDailyHours(attData[4].trim(), attData[5].trim());
            }
        }
        reader.close();
        return totalHours;
    }

    // This method computes the number of hours an employee worked in a single day
    // based on their login and logout times. It enforces a work window of 8:00 AM to 5:00 PM,
    // capping any early arrivals or late departures to those boundaries. A 10-minute grace
    // period is applied at the start — logins at or before 8:10 AM are treated as exactly
    // 8:00 AM, while logins after 8:10 AM use the actual time. A one-hour lunch break is
    // automatically deducted if total time worked exceeds 60 minutes. The result is returned
    // as a decimal value representing hours worked.
    public static double computeDailyHours(String logIn, String logOut) {
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("H:mm");
        LocalTime start = LocalTime.parse(logIn, timeFormat);
        LocalTime end = LocalTime.parse(logOut, timeFormat);
        LocalTime workStart = LocalTime.of(8, 0);
        LocalTime workEnd = LocalTime.of(17, 0);
        LocalTime graceEnd = LocalTime.of(8, 10); // 10-minute grace period

        // If login is early or within grace period (8:00–8:10), treat as 8:00.
        // If login is 8:11 or later, use actual login time.
        LocalTime effectiveStart;
        if (!start.isAfter(graceEnd)) {
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

    // This method determines the employee's SSS contribution by comparing their gross monthly
    // salary against a tiered bracket table defined by SSS. Each bracket
    // maps a salary range to a fixed contribution amount.
    // The method returns the fixed contribution amount corresponding to the matching bracket.
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

    // This method computes the employee's share of the monthly PhilHealth premium.
    // The total premium rate is 3% of the gross salary, split equally between the employee
    // and employer, making the employee's effective rate 1.5%. Salaries below ₱10,000 incur
    // no contribution. Salaries from ₱10,000 up to ₱59,999.99 are charged at 1.5% of the
    // gross salary. Salaries at ₱60,000 and above are capped at ₱900.00, which is the
    // employee's share of the total premium of ₱1,800.00.
    public static double computePhilHealth(double grossSalary) {
        if (grossSalary < 10000) {
            return 0.00;
        } else if (grossSalary <= 59999.99) {
            return grossSalary * 0.015; // employee share is 50% of 3%
        } else {
            return 900.00;
        }
    }

    // This method calculates the employee's monthly Pag-IBIG contribution based on
    // their gross salary. Salaries below ₱1,000 incur no contribution. Salaries from ₱1,000
    // to ₱1,500 are charged at 1% of the gross salary, while salaries above ₱1,500 are
    // charged at 2%. Regardless of the computed rate, the contribution is capped at a maximum
    // of ₱100.00 per month, and that capped value is returned if the calculation exceeds it.
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

    // This method calculates the employee's monthly withholding tax based on the
    // tax table applied to the taxable income. Income at or below ₱20,832
    // is fully tax-exempt. Each bracket above that applies its corresponding rate only
    // to the amount exceeding that bracket's lower bound. The computed tax is then returned.
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

    // This method parses a single line from a CSV file into an array of string fields,
    // correctly handling fields that contain commas within double quotes. It first counts
    // the number of fields by scanning for unquoted commas, then performs a second pass
    // to extract each field's value. When a double-quote character is encountered, the
    // method toggles an inQuotes flag to tell apart commas inside quotes from commas
    // outside them. The resulting array of fields is returned for further processing.
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

    // This method converts a money-formatted string value into a plain double. It works
    // by first trimming any leading or trailing whitespace, then removing all commas and
    // double-quote characters that may be present due to CSV formatting, before passing
    // the cleaned string to Double.parseDouble() for conversion.
    public static double parseMoney(String value) {
        return Double.parseDouble(value.trim().replace(",", "").replace("\"", ""));
    }

    // This method searches the employee CSV file for a record matching the given employee
    // number. It reads each line of the file, parses it into fields using parseCSVLine(),
    // and compares the first field against the provided employee number. If a match is found,
    // the file is closed and the data for that employee is returned. If no match
    // exists after reading all records, the file is closed and null is returned.
    public static String[] findEmployee(String empNum) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(employeeData));
        reader.readLine(); // Skip the first line
        String line;
        while ((line = reader.readLine()) != null) {
            String[] empData = parseCSVLine(line);
            if (empData[0].trim().equals(empNum)) {
                reader.close();
                return empData;
            }
        }
        reader.close();
        return null;
    }
}
