package mods.defeatedcrow.plugin.nei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import mods.defeatedcrow.recipe.ProsessorRecipeRegister.ProsessorRecipe;
import mods.defeatedcrow.api.recipe.RecipeRegisterManager;
import mods.defeatedcrow.client.gui.GuiIceMaker;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class ProcessorRecipeHandler extends TemplateRecipeHandler {
	
	public int[][] stackorder = new int[][]{
            {0, 0},
            {1, 0},
            {0, 1},
            {1, 1},
            {0, 2},
            {1, 2},
            {2, 0},
            {2, 1},
            {2, 2}};
	
	private List<ProsessorRecipe> recipes;
	
	private List<ProsessorRecipe> recipeLoader() {
		if (RecipeRegisterManager.prosessorRecipe.getRecipes() != null && !RecipeRegisterManager.prosessorRecipe.getRecipes().isEmpty()) {
			this.recipes = (List<ProsessorRecipe>) RecipeRegisterManager.prosessorRecipe.getRecipes();
		}
		return this.recipes;
	}
	
	public class RecipeCacher extends CachedRecipe {
		
		public ArrayList<PositionedStack> input;
		public PositionedStack result;
		public PositionedStack leave;
		
		public RecipeCacher() {
			input = new ArrayList<PositionedStack>();
		}
		
		public RecipeCacher(ItemStack[] out) {
			this();
			this.result= new PositionedStack(out[0], 113, 24);
			if (out[1] != null)
			{
				this.leave = new PositionedStack(out[1], 140, 24);
			}
		}

		public RecipeCacher(List<?> in, ItemStack[] out) {
			this(out);
			setInput(in);	
		}
		
		public void setInput(List<?> items) {
            input.clear();
            for (int ingred = 0; ingred < items.size(); ingred++) {
                PositionedStack stack = new PositionedStack(items.get(ingred), 28 + stackorder[ingred][0] * 18, 5 + stackorder[ingred][1] * 18);
                stack.setMaxSize(1);
                input.add(stack);
            }
        }

		@Override
		public PositionedStack getResult() {
			return this.result;
		}
		
		@Override
		public List<PositionedStack> getIngredients()
        {
            return getCycledIngredients(cycleticks / 20, input);
        }
		
		public PositionedStack getOtherStack()
        {
            return this.leave;
        }
		
	}
	
	public PositionedStack getResult() {
	    return null;
	}

	@Override
	public Class<? extends GuiContainer> getGuiClass() {
	    return GuiIceMaker.class;
	}
	
	@Override
	public String getOverlayIdentifier() {
	  return "DCsProcessor";
	}
	
	@Override
	public void loadTransferRects() {
	    transferRects.add(new TemplateRecipeHandler.RecipeTransferRect(new Rectangle(80, 25, 20, 20), "DCsProcessor"));
	}
	
	@Override
    public void loadCraftingRecipes(String outputId, Object... results)
    {
        if(outputId.equals("DCsProcessor"))
        {
            List<ProsessorRecipe> recipes = this.recipeLoader();

            if(recipes == null || recipes.isEmpty())return;
            for(ProsessorRecipe recipe : recipes)
            {
                List<Object> in = recipe.getProsessedInput();
                boolean flag = recipe.isFoodRecipe();
            	for(Object ret : in) {
            		if (ret instanceof List){
        				List<ItemStack> items = (List<ItemStack>) ret;
        				if (items.isEmpty())flag = false;
        			}
            	}
                
                ItemStack[] out = new ItemStack[] {recipe.getOutput(),recipe.getSecondary()};
                if (flag) arecipes.add(new RecipeCacher(in,out));
            }
        }
        else
        {
            super.loadCraftingRecipes(outputId, results);
        }
    }
	
	@Override
    public void loadCraftingRecipes(ItemStack result)
    {

		List<ProsessorRecipe> recipes = this.recipeLoader();

		if(recipes == null || recipes.isEmpty())return;
        for(ProsessorRecipe recipe : recipes)
        {
        	List<Object> in = recipe.getProsessedInput();
        	//input中にnullがないかチェックする。鉱石辞書レシピ用。
        	boolean flag = recipe.isFoodRecipe();
        	for(Object ret : in) {
        		if (ret instanceof List){
    				List<ItemStack> items = (List<ItemStack>) ret;
    				if (items.isEmpty())flag = false;
    			}
        	}
            ItemStack[] out = new ItemStack[] {recipe.getOutput(),recipe.getSecondary()};
            if(flag && NEIServerUtils.areStacksSameType(out[0], result))
            {
                arecipes.add(new RecipeCacher(in, out));
            }
        }
    }
	
	@Override
    public void loadUsageRecipes(ItemStack ingredient)
    {

		List<ProsessorRecipe> recipes = this.recipeLoader();

		if(recipes == null || recipes.isEmpty())return;
        for(ProsessorRecipe recipe : recipes)
        {
        	if (recipe == null)continue;
        	
        	List<Object> in = recipe.getProsessedInput();
            ItemStack[] out = new ItemStack[] {recipe.getOutput(),recipe.getSecondary()};
            
            boolean flag = recipe.isFoodRecipe() && this.contain(in, ingredient);
            
            
            if (flag){ 
            	RecipeCacher cache = new RecipeCacher(in, out);
            	cache.setIngredientPermutation(cache.input, ingredient);
            	arecipes.add(cache);
            }
        }
    }
	
	private boolean contain(List<Object> in, ItemStack check)
	{
		boolean flag1 = false;
		if (check == null) return false;
		
		for (Object ret : in)
		{
			if (ret == null)continue;
			
			if (ret instanceof ItemStack){
				ItemStack input = (ItemStack) ret;
				if (NEIServerUtils.areStacksSameType(input, check))
					flag1 = true;
			}
			else if (ret instanceof List){
				List<ItemStack> items = (List<ItemStack>) ret;
				if (items.isEmpty())continue;
				for (ItemStack item : items){
					if (NEIServerUtils.areStacksSameType(item, check))
						flag1 = true;
				}
			}
		}
		
		return flag1;
	}

	@Override
	public String getRecipeName() {
		return "Food Processor";
	}

	@Override
	public String getGuiTexture() {
		return "defeatedcrow:textures/gui/foodprocessorgui_nei.png";
	}
	
	@Override
    public void drawExtras(int recipe)
    {
        drawProgressBar(83, 24, 176, 0, 24, 16, 32, 0);
    }

}
