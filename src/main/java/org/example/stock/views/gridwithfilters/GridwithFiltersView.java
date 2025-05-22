package org.example.stock.views.gridwithfilters;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.example.stock.services.StockService;
import org.example.stock.services.TickerData;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Grid with Filters")
@Route("grid-with-filters")
@Menu(order = 2, icon = LineAwesomeIconUrl.FILTER_SOLID)
@Uses(Icon.class)
public class GridwithFiltersView extends Div {

    private Grid<TickerData> grid;

    private final StockService stockService;

    public GridwithFiltersView(StockService stockService) {
        this.stockService = stockService;
        setSizeFull();
        addClassNames("gridwith-filters-view");
        TextField searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Search");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        VerticalLayout layout = new VerticalLayout(createGrid());
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(searchField, layout);

        GridListDataView<TickerData> dataView = grid.setItems(stockService.getData());
        searchField.addValueChangeListener(e -> dataView.refreshAll());


        dataView.addFilter(data -> {
            String searchTerm = searchField.getValue().trim();

            if (searchTerm.isEmpty())
                return true;

            return data.symbol().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    data.name().toLowerCase().contains(searchTerm.toLowerCase());
        });

    }

    private Component createGrid() {
        grid = new Grid<>(TickerData.class, false);
        grid.addColumn("symbol").setAutoWidth(true);
        grid.addColumn("name").setAutoWidth(true);

        grid.setItems(stockService.getData());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

}
