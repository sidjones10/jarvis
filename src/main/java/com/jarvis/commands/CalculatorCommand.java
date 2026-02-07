package com.jarvis.commands;

import com.jarvis.core.Command;
import java.util.Stack;

public class CalculatorCommand implements Command {

    @Override
    public String getName() {
        return "calc <expression>";
    }

    @Override
    public String getDescription() {
        return "Evaluates a math expression (e.g., calc 2 + 3 * 4).";
    }

    @Override
    public String[] getTriggers() {
        return new String[]{"calc ", "calculate ", "compute "};
    }

    @Override
    public String execute(String input) {
        String expression = input.replaceFirst("(?i)(calc|calculate|compute)\\s+", "").trim();
        if (expression.isEmpty()) {
            return "Please provide an expression to evaluate, sir. Example: calc 2 + 3 * 4";
        }
        try {
            double result = evaluate(expression);
            if (result == (long) result) {
                return String.format("The result is %d, sir.", (long) result);
            }
            return String.format("The result is %.6f, sir.", result);
        } catch (Exception e) {
            return "I couldn't evaluate that expression, sir. Please check the syntax.";
        }
    }

    private double evaluate(String expr) {
        expr = expr.replaceAll("\\s+", "");
        Stack<Double> numbers = new Stack<>();
        Stack<Character> operators = new Stack<>();
        int i = 0;

        while (i < expr.length()) {
            char c = expr.charAt(i);

            if (c == '(') {
                operators.push(c);
                i++;
            } else if (c == ')') {
                while (operators.peek() != '(') {
                    numbers.push(applyOp(operators.pop(), numbers.pop(), numbers.pop()));
                }
                operators.pop();
                i++;
            } else if (Character.isDigit(c) || c == '.') {
                StringBuilder sb = new StringBuilder();
                while (i < expr.length() && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    sb.append(expr.charAt(i++));
                }
                numbers.push(Double.parseDouble(sb.toString()));
            } else if (c == '+' || c == '-' || c == '*' || c == '/' || c == '^') {
                // Handle negative numbers at start or after operator/open paren
                if (c == '-' && (numbers.isEmpty() || (i > 0 && (expr.charAt(i - 1) == '(' || isOperator(expr.charAt(i - 1)))))) {
                    StringBuilder sb = new StringBuilder("-");
                    i++;
                    while (i < expr.length() && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                        sb.append(expr.charAt(i++));
                    }
                    numbers.push(Double.parseDouble(sb.toString()));
                } else {
                    while (!operators.isEmpty() && hasPrecedence(c, operators.peek())) {
                        numbers.push(applyOp(operators.pop(), numbers.pop(), numbers.pop()));
                    }
                    operators.push(c);
                    i++;
                }
            } else {
                i++;
            }
        }

        while (!operators.isEmpty()) {
            numbers.push(applyOp(operators.pop(), numbers.pop(), numbers.pop()));
        }

        return numbers.pop();
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^';
    }

    private boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') return false;
        if (op1 == '^') return false;
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) return false;
        return true;
    }

    private double applyOp(char op, double b, double a) {
        return switch (op) {
            case '+' -> a + b;
            case '-' -> a - b;
            case '*' -> a * b;
            case '/' -> {
                if (b == 0) throw new ArithmeticException("Division by zero");
                yield a / b;
            }
            case '^' -> Math.pow(a, b);
            default -> 0;
        };
    }
}
