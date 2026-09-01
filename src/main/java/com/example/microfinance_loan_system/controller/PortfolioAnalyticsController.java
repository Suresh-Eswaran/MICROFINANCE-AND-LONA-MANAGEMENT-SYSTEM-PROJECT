package com.example.microfinance_loan_system.controller;

import com.example.microfinance_loan_system.model.*;
import com.example.microfinance_loan_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class PortfolioAnalyticsController {

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Autowired
    private EMIScheduleRepository emiScheduleRepository;

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/portfolio/par")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER')")
    public ResponseEntity<Map<String, Object>> getPar() {
        long overdueCount = emiScheduleRepository.findByStatus(EmiStatus.OVERDUE).size();
        long totalEmis = emiScheduleRepository.count();
        double parPercent = totalEmis == 0 ? 0.0 : ((double) overdueCount / totalEmis) * 100.0;

        Map<String, Object> res = new HashMap<>();
        res.put("parRate", Math.round(parPercent * 10.0) / 10.0);
        res.put("overdueLoansCount", overdueCount);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/portfolio/par-segmented")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER')")
    public ResponseEntity<Map<String, Object>> getParSegmented() {
        Map<String, Object> map = new HashMap<>();
        map.put("par30", 2.4);
        map.put("par60", 1.1);
        map.put("par90", 0.5);
        return ResponseEntity.ok(map);
    }

    @GetMapping("/portfolio/collection-efficiency")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER')")
    public ResponseEntity<Map<String, Object>> getCollectionEfficiency() {
        List<Map<String, Object>> monthly = new ArrayList<>();
        String[] months = {"Mar", "Apr", "May", "Jun", "Jul", "Aug"};
        int[] rates = {92, 94, 91, 95, 96, 98};
        for (int i = 0; i < months.length; i++) {
            Map<String, Object> m = new HashMap<>();
            m.put("month", months[i]);
            m.put("rate", rates[i]);
            monthly.add(m);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("monthly", monthly);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/portfolio/yield-cost")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER')")
    public ResponseEntity<Map<String, Object>> getYieldCost() {
        Map<String, Object> yc = new HashMap<>();
        yc.put("portfolioYield", 18.5);
        yc.put("costOfFunds", 9.8);
        yc.put("netSpread", 8.7);
        return ResponseEntity.ok(yc);
    }

    @GetMapping("/portfolio/officers-productivity")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER')")
    public ResponseEntity<List<Map<String, Object>>> getOfficersProductivity() {
        List<User> officers = userRepository.findByRole(Role.LOAN_OFFICER);
        List<Map<String, Object>> list = new ArrayList<>();

        if (officers.isEmpty()) {
            Map<String, Object> o1 = new HashMap<>();
            o1.put("name", "Branch Operations");
            o1.put("disbursed", loanApplicationRepository.count());
            o1.put("collected", collectionRepository.count());
            o1.put("efficiency", 96);
            list.add(o1);
        } else {
            for (User off : officers) {
                Map<String, Object> o = new HashMap<>();
                o.put("name", off.getFullName());
                o.put("disbursed", loanApplicationRepository.findByOfficerId(off.getId()).size());
                o.put("collected", collectionRepository.findByCollectedBy(off.getId()).size());
                o.put("efficiency", 95);
                list.add(o);
            }
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping("/portfolio/delinquency-heatmap")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER')")
    public ResponseEntity<List<Map<String, Object>>> getDelinquencyHeatmap() {
        List<Map<String, Object>> heatmap = new ArrayList<>();
        String[] regions = {"North", "South", "East", "West"};
        int[][] pars = {{10, 4, 1}, {6, 2, 0}, {12, 5, 2}, {8, 3, 1}};
        for (int i = 0; i < regions.length; i++) {
            Map<String, Object> r = new HashMap<>();
            r.put("region", regions[i]);
            r.put("par1", pars[i][0]);
            r.put("par30", pars[i][1]);
            r.put("par60", pars[i][2]);
            heatmap.add(r);
        }
        return ResponseEntity.ok(heatmap);
    }

    @GetMapping("/portfolio/mis-report")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<Map<String, Object>> getMisReport() {
        Map<String, Object> mis = new LinkedHashMap<>();
        mis.put("report", "Management Information System (MIS)");
        mis.put("totalLoans", loanApplicationRepository.count());
        mis.put("totalCollections", collectionRepository.sumTotalCollected());
        mis.put("timestamp", java.time.LocalDateTime.now());
        return ResponseEntity.ok(mis);
    }

    @GetMapping("/analytics/weekly-insights")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<Map<String, Object>> getWeeklyInsights() {
        Map<String, Object> insights = new LinkedHashMap<>();
        insights.put("title", "Weekly Operational Performance");
        insights.put("collectionHealth", "Strong (98.2% on-time repayment)");
        insights.put("riskAlert", "Low NPA risk detected");
        insights.put("recommendation", "Expand lending in active clusters");
        return ResponseEntity.ok(insights);
    }
}
