package com.example;

public class Main {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("{\"status\":\"error\",\"message\":\"Please provide exactly three arguments: <operation> <num1> <num2>.\"}");
            return;
        }

        String operation = args[0];
        int a, b;

        try {
            a = Integer.parseInt(args[1]);
            b = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.out.println("{\"status\":\"error\",\"message\":\"Both numbers must be integers.\"}");
            return;
        }

        int result;
        boolean validOperation = true;

        switch (operation) {
    case "+":
        result = add(a, b);
        break;
    case "-":
        result = subtract(a, b);
        break;
    case "*":
        result = multiply(a, b);
        break;
    case "/":
        if (b == 0) {
            System.out.println("{\"status\":\"error\",\"message\":\"Division by zero is not allowed.\"}");
            return;
        }
        result = divide(a, b);
        break;
    default:
        validOperation = false;
        result = 0;
        break;
}



        if (!validOperation) {
            System.out.println("{\"status\":\"error\",\"message\":\"Unknown operation. Use +, -, or *\"}");
        } else {
            String jsonResult = "{\"status\":\"success\",\"operation\":\"" + operation + "\",\"input_a\":" + a + ",\"input_b\":" + b + ",\"result\":" + result + "}";
            System.out.println(jsonResult);
        }
    }

    public static int add(int a, int b) {
        return a + b;
    }

    public static int subtract(int a, int b) {
        return a - b;
    }

    public static int multiply(int a, int b) {
        return a * b;
    }

    public static int divide(int a, int b) {
    	return a / b;
    }

}
