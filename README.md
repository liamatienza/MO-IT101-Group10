# MO-IT101-Group10
## Team Details
**John Florence Guillermo**
- **System Logic:** Led the employee profile and salary summary layout, defined time-log variables, developed core logic for calculating total hours worked, managed conditional checks for Log-in timestamps to ensure data accuracy, and implemented CSV parsing to correctly handle quoted fields and formatted money values.

**Liam Andryl Atienza**
- **Data & Display:** Declared essential variables for employee profiles, designed the display format/template, and implemented the logic to fetch employee data.
- **Documentation:** Performed final refinement of code comments for clarity.

**Charmaine Ylanan**
- **Payroll Logic:** Defined hourly rate variables and implemented the primary gross salary calculation.

**CJ Bactong**
- **Tax, Finance & Output:** Implemented complex BIR tax logic to compute withholding tax, finalized net pay calculations, and standardized the final currency output for the system.

**Jamil Latumbo**
- **Grace Period Logic:** Implemented the late deduction logic, treating logins at or before 8:10 AM as on-time and using the actual login time for logins after 8:10 AM.

## Program Details
The MotorPH Payroll System automates employee payroll calculation in compliance with Philippine labor and tax regulations through four stages:

- **Attendance Integration:** Computes daily hours from Time-In/Time-Out timestamps. Logins within the 8:00-8:10 AM grace period are treated as 8:00 AM; logins after 8:10 AM use the actual time.

- **Gross Salary Computation:** Calculates earnings twice a month (1st-15th and 16th-last day of the month).

- **Statutory & Tax Deductions:** Computes mandatory SSS, PhilHealth, and Pag-IBIG contributions from the monthly gross, then applies withholding tax on the taxable income (gross minus the three contributions).

- **Final Output:** Displays the employee profile, gross salary breakdown, itemized deductions (SSS, PhilHealth, Pag-IBIG, tax), and the Net Semi-Monthly Pay per period.


## [Project Plan Link](https://docs.google.com/spreadsheets/d/1IrsTm8_1G0dqR1G7RoXgl_EKcljFTrMbsPZg3_LuTcM/edit)
