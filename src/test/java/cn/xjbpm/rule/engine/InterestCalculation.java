package cn.xjbpm.rule.engine;

import lombok.SneakyThrows;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/10/26
 * 利息计算等额本金
 */
public class InterestCalculation {

    @SneakyThrows
    public static void main(String[] args) {
        int loanTermInMonths = 180; // 贷款期限，以月为单位
        double loanAmount = 390000.0; // 贷款金额
        double annualInterestRate = 5.39; // 年利率
        int numberOfRepaymentPeriods = 80; // 已还款期数
        // 将年利率转换为月利率
        double monthlyInterestRate = annualInterestRate / 12 / 100;
        // 计算每月本金还款额
        double monthlyPrincipalPayment = loanAmount / loanTermInMonths;

        DecimalFormat df = new DecimalFormat("#.##"); // 用于格式化输出
        DecimalFormat df1 = new DecimalFormat("#.######"); // 用于格式化输出
        System.out.println("贷款金额: " + loanAmount + " 元");
        System.out.println("年利率: " + annualInterestRate + "%");
        System.out.println("贷款期限: " + loanTermInMonths + " 个月\n");
        System.out.println("月份\t本金还款\t利息还款\t当月还款\t剩余本金");
        DoubleAdder 已还本金 = new DoubleAdder();
        DoubleAdder 已还利息 = new DoubleAdder();
        DoubleAdder 待还本金 = new DoubleAdder();
        DoubleAdder 待还利息 = new DoubleAdder();
        DoubleAdder 总利息 = new DoubleAdder();
        List<List<String>> tableRowContents = new ArrayList<>();
        for (int month = 1; month <= loanTermInMonths; month++) {

            // 计算利息还款额
            double monthlyInterestPayment = (loanAmount - (month - 1) * monthlyPrincipalPayment) * monthlyInterestRate;
            // 当前还款
            double currentRepayment = monthlyPrincipalPayment + monthlyInterestPayment;
            // 剩余本金
            double remainingPrincipal = loanAmount - (month * monthlyPrincipalPayment);

            if (month <= numberOfRepaymentPeriods) {
                已还本金.add(monthlyPrincipalPayment);
                已还利息.add(monthlyInterestPayment);
            } else {
                待还本金.add(monthlyPrincipalPayment);
                待还利息.add(monthlyInterestPayment);
            }
            总利息.add(monthlyInterestPayment);
            // 输出结果
            System.out.println(month + "\t" + df.format(monthlyPrincipalPayment) + " 元\t"
                    + df.format(monthlyInterestPayment) + " 元\t"
                    + df.format(currentRepayment) + " 元\t"
                    + df.format(remainingPrincipal) + " 元"
            );
            tableRowContents.add(Arrays.asList(
                    String.valueOf(month), df.format(monthlyPrincipalPayment), df.format(monthlyInterestPayment), df.format(currentRepayment), df.format(remainingPrincipal)
            ));
        }

        System.out.println(String.format("已还期数:%s 剩余期数:%s 已还本金:%s 已还利息:%s 待还本金:%s 待还利息:%s 总利息:%s",
                numberOfRepaymentPeriods, loanTermInMonths - numberOfRepaymentPeriods,
                df.format(已还本金.doubleValue()),
                df.format(已还利息.doubleValue()),
                df.format(待还本金.doubleValue()),
                df.format(待还利息.doubleValue()),
                df.format(总利息.doubleValue())
        ));


        tableRowContents.add(Arrays.asList("已还本金", "已还利息", "待还本金", "待还利息", "总利息"));
        tableRowContents.add(Arrays.asList(
                df.format(已还本金.doubleValue()),
                df.format(已还利息.doubleValue()),
                df.format(待还本金.doubleValue()),
                df.format(待还利息.doubleValue()),
                df.format(总利息.doubleValue()))
        );

        tableRowContents.add(Arrays.asList("贷款期限", "已还款期数", "贷款金额", "年利率", "月利率"));
        tableRowContents.add(Arrays.asList(
                String.valueOf(loanTermInMonths),
                String.valueOf(numberOfRepaymentPeriods),
                df.format(loanAmount),
                df.format(annualInterestRate),
                df1.format(monthlyInterestRate)
        ));

        List<DrawTableUtil.Cell> headCells = new ArrayList<>();

        headCells.add(new DrawTableUtil.Cell(1, 1, 100, 1).setTextAlign(true).setContent("还款期数"));
        headCells.add(new DrawTableUtil.Cell(1, 2, 100, 1).setTextAlign(true).setContent("本金还款"));
        headCells.add(new DrawTableUtil.Cell(1, 3, 100, 1).setTextAlign(true).setContent("利息还款"));
        headCells.add(new DrawTableUtil.Cell(1, 4, 100, 1).setTextAlign(true).setContent("当月还款"));
        headCells.add(new DrawTableUtil.Cell(1, 5, 100, 1).setTextAlign(true).setContent("剩余本金"));

        DrawTableUtil.Table table = new DrawTableUtil.Table()
                .setCellFont(new Font("宋体", Font.PLAIN, 14))
                .setHeadCells(headCells)
                .setHeaderFont(new Font("宋体", Font.PLAIN, 18))
                .setHeaderBackGroundColor(Color.pink);
        BufferedImage image = DrawTableUtil.drawTable(table, tableRowContents);
        ImageIO.write(image, "png", new File("F://temp//temp.png"));
    }

}
