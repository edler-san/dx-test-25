package org.example.stock.views.dashboard;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.crud.BinderCrudEditor;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.crud.CrudFilter;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.BoxSizing;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.example.stock.services.PortfolioItem;
import org.example.stock.services.PortfolioService;
import org.example.stock.services.StockService;
import org.vaadin.lineawesome.LineAwesomeIconUrl;


@PageTitle("Dashboard")
@Route("")
@Menu(order = 0, icon = LineAwesomeIconUrl.CHART_AREA_SOLID)
public class DashboardView extends Main {
   
    private PortfolioService portfolioService;
    private StockService stockService;

public DashboardView(PortfolioService portfolioService, StockService stockService) {
        this.portfolioService = portfolioService;
        this.stockService = stockService;
        addClassName("dashboard-view");
        
        Board board = new Board();
        board.addRow(createHighlight("Current users", "745", 33.7), createHighlight("View events", "54.6k", -112.45),
                createHighlight("Conversion rate", "18%", 3.9), createHighlight("Custom metric", "-123.45", 0.0));
        board.addRow(createPortfolio(), createValueDisplay());
        add(board);
    }

    private Component createHighlight(String title, String value, Double percentage) {
        VaadinIcon icon = VaadinIcon.ARROW_UP;
        String prefix = "";
        String theme = "badge";

        if (percentage == 0) {
            prefix = "±";
        } else if (percentage > 0) {
            prefix = "+";
            theme += " success";
        } else if (percentage < 0) {
            icon = VaadinIcon.ARROW_DOWN;
            theme += " error";
        }

        H2 h2 = new H2(title);
        h2.addClassNames(FontWeight.NORMAL, Margin.NONE, TextColor.SECONDARY, FontSize.XSMALL);

        Span span = new Span(value);
        span.addClassNames(FontWeight.SEMIBOLD, FontSize.XXXLARGE);

        Icon i = icon.create();
        i.addClassNames(BoxSizing.BORDER, Padding.XSMALL);

        Span badge = new Span(i, new Span(prefix + percentage.toString()));
        badge.getElement().getThemeList().add(theme);

        VerticalLayout layout = new VerticalLayout(h2, span, badge);
        layout.addClassName(Padding.LARGE);
        layout.setPadding(false);
        layout.setSpacing(false);
        return layout;
    }

    private Component createPortfolio() {

        // portfolioService.save(new PortfolioItem("AAPL", 10));
        // portfolioService.save(new PortfolioItem("GOOG", 25));
        // portfolioService.save(new PortfolioItem("TL0", 25));
        // System.out.println(portfolioService.findAll());
        // Header
        HorizontalLayout header = createHeader("Portfolio", "");
        // Grid
        Crud<PortfolioItem> crud = new Crud<>(PortfolioItem.class, createEditor());
        crud.getGrid().removeColumnByKey("id");
        crud.getGrid().addThemeVariants(GridVariant.LUMO_NO_BORDER);
        crud.getGrid().setAllRowsVisible(true);

        // Create a proper data provider for Crud that handles CrudFilter
        CallbackDataProvider<PortfolioItem, CrudFilter> dataProvider = 
            DataProvider.fromFilteringCallbacks(
                // Fetching items with potential filtering
                query -> {
                    // Get the filter from the query
                    CrudFilter filter = query.getFilter().orElse(null);
                    
                    // Get all items from portfolio service
                    List<PortfolioItem> list = portfolioService.findAll();
                    
                    // Apply filtering if filter exists
                    if (filter != null && !filter.getConstraints().isEmpty()) {
                        // Implement filtering based on the filter constraints
                        filter.getConstraints().forEach((key, value) -> {
                            String filterValue = value.toString().toLowerCase();
                            
                            if (key.equals("symbol")) {
                                list.stream().filter(item -> 
                                    item.getSymbol().toLowerCase().contains(filterValue)).toList();
                            } else if (key.equals("quantity")) {
                                try {
                                    int intValue = Integer.parseInt(filterValue);
                                    list.stream().filter(item -> item.getQuantity() == intValue).toList();
                                } catch (NumberFormatException e) {
                                    // Invalid number format, ignore this filter
                                }
                            }
                        });
                    }
                    
                    // Apply sorting if needed
                    if (query.getSortOrders().size() > 0) {
                        // Implement sorting logic here
                    }
                    
                    // Apply offset and limit
                    return list.stream().skip(query.getOffset()).limit(query.getLimit());
                },
                // Counting items with potential filtering
                query -> {
                    // Get the filter from the query
                    CrudFilter filter = query.getFilter().orElse(null);
                    
                    // Get all items
                    List<PortfolioItem> items = portfolioService.findAll();
                    
                    // Apply filtering if filter exists
                    if (filter != null && !filter.getConstraints().isEmpty()) {
                        // Similar filtering logic as above
                        filter.getConstraints().forEach((key, value) -> {
                            String filterValue = value.toString().toLowerCase();
                            
                            if (key.equals("symbol")) {
                                items.stream()
                                    .filter(item -> item.getSymbol().toLowerCase().contains(filterValue))
                                    .collect(Collectors.toList());
                            } else if (key.equals("quantity")) {
                                try {
                                    int intValue = Integer.parseInt(filterValue);
                                    items.stream()
                                        .filter(item -> item.getQuantity() == intValue)
                                        .collect(Collectors.toList());
                                } catch (NumberFormatException e) {
                                    // Invalid number format, ignore this filter
                                }
                            }
                        });
                    }
                    
                    return items.size();
                }
            );

        // Set the DataProvider and configure CRUD operations
        crud.setDataProvider(dataProvider);

        crud.addSaveListener(event -> {
            PortfolioItem item = event.getItem();
            portfolioService.save(item);
            crud.getDataProvider().refreshAll(); // Refresh the grid
        });

        VerticalLayout pf = new VerticalLayout(header, crud);
        pf.addClassName(Padding.LARGE);
        pf.setPadding(false);
        pf.setSpacing(false);
        pf.getElement().getThemeList().add("spacing-l");
        pf.getStyle().set("overflow", "auto");
        return pf;
    }

    private CrudEditor<PortfolioItem> createEditor() {
        TextField symbol = new TextField("Symbol");
        IntegerField quantity = new IntegerField("Quantity");
        FormLayout form = new FormLayout(symbol, quantity);

        Binder<PortfolioItem> binder = new Binder<>(PortfolioItem.class);
        binder.forField(symbol).asRequired().bind(PortfolioItem::getSymbol,PortfolioItem::setSymbol);
        binder.forField(quantity).asRequired().withValidator(number -> number > 0, "Must be greater than 0")
                .bind(PortfolioItem::getQuantity, PortfolioItem::setQuantity);

        return new BinderCrudEditor<>(binder, form);
    }

    private Component createValueDisplay() {
        HorizontalLayout header = createHeader("Portfolio", "");
        Button button = new Button("Refresh", new Icon(VaadinIcon.REFRESH));
        button.getElement().getThemeList().add("primary");
        DatePicker datePicker = new DatePicker("Date");
        datePicker.setRequiredIndicatorVisible(true);
        datePicker.setMax(LocalDate.now(ZoneId.systemDefault()).minusDays(1L));

        // Chart
        Chart chart = new Chart(ChartType.PIE);
        Configuration conf = chart.getConfiguration();
        conf.getChart().setStyledMode(true);
        chart.setThemeName("gradient");

        button.addClickListener(e -> {
            if(datePicker.isEmpty()) {
                Notification.show("Please select a date");
                return;
            }
            button.setEnabled(false);
            DataSeries series = new DataSeries();

            portfolioService.findAll().forEach(item -> {
                Date date = Date.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                var stockData = stockService.getStockPrice(item.getSymbol(), date);
                series.add(new DataSeriesItem(item.getSymbol(),stockData.getClose()*item.getQuantity()));
            });

            // series.add(new DataSeriesItem("System 1", Math.random() * 100));
            // series.add(new DataSeriesItem("System 2", Math.random() * 100));
            // series.add(new DataSeriesItem("System 3", Math.random() * 100));
            // series.add(new DataSeriesItem("System 4", Math.random() * 100));
            // series.add(new DataSeriesItem("System 5", Math.random() * 100));
            // series.add(new DataSeriesItem("System 6", Math.random() * 100));
            //conf.addSeries(series);
            conf.setSeries(series);
            chart.drawChart();
            enableComponent(button, 60);
        });

        // Add it all together
        VerticalLayout serviceHealth = new VerticalLayout(header, button, datePicker, chart);
        serviceHealth.addClassName(Padding.LARGE);
        serviceHealth.setPadding(false);
        serviceHealth.setSpacing(false);
        serviceHealth.getElement().getThemeList().add("spacing-l");
        return serviceHealth;
    }

    private void enableComponent(Button button, int seconds) {
        new Thread(() -> {
            try {
                Thread.sleep(seconds*1000L);
                
                // UI update must be run in the UI thread
                button.getUI().ifPresent(ui -> {
                    ui.access(() -> {
                        button.setEnabled(true);
                    });
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private HorizontalLayout createHeader(String title, String subtitle) {
        H2 h2 = new H2(title);
        h2.addClassNames(FontSize.XLARGE, Margin.NONE);

        Span span = new Span(subtitle);
        span.addClassNames(TextColor.SECONDARY, FontSize.XSMALL);

        VerticalLayout column = new VerticalLayout(h2, span);
        column.setPadding(false);
        column.setSpacing(false);

        HorizontalLayout header = new HorizontalLayout(column);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setSpacing(false);
        header.setWidthFull();
        return header;
    }

}
