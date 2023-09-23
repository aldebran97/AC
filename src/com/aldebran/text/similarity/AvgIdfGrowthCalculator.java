package com.aldebran.text.similarity;

import java.io.Serializable;

/**
 * 基础均匀线性增长 平均IDF增加值计算器
 * 可继承此类实现，构造tan(x)、sigmoid(x)...等。对称中心在（gramAvgIdf , k * basicGrowthValue）的曲线
 *
 * @author aldebran
 * @since 2023-09-23
 */
public class AvgIdfGrowthCalculator implements Serializable {

    public double basicGrowthValue;

    public double gramAvgIdf;

    public double gramMinIdf;

    public double gramMaxIdf;

    public double titleIdfRate;

    public double idfGrowthK;

    public double calc(double gramIdf, boolean isTitle) {
        if (isTitle) {
            gramIdf *= titleIdfRate;
        }
        return 10 * gramIdf / gramAvgIdf * basicGrowthValue;
    }

    public void update(double basicGrowthValue, double gramAvgIdf, double gramMinIdf, double gramMaxIdf, double titleIdfRate,
                       double idfGrowthK) {
        this.basicGrowthValue = basicGrowthValue;
        this.gramAvgIdf = gramAvgIdf;
        this.gramMinIdf = gramMinIdf;
        this.gramMaxIdf = gramMaxIdf;
        this.titleIdfRate = titleIdfRate;
        this.idfGrowthK = idfGrowthK;
    }
}
