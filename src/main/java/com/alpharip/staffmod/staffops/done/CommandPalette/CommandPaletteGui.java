package com.alpharip.staffmod.CommandPalette;

import com.alpharip.staffmod.gui.AlphaClientHomeMenu;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import java.io.IOException;
import java.util.*;

public final class CommandPaletteGui extends GuiScreen {
    private static final int ACCENT=0xFFFFC83D, ROW_HEIGHT=32;
    private static final String[] CATEGORIES={"All","Recent","Favorites","Modules","Screens","Waypoints","Commands"};
    private final GuiScreen parent;
    private GuiTextField searchField;
    private List<Action> actions=Collections.emptyList(), filteredCache=Collections.emptyList();
    private boolean filtersDirty=true;
    private String category="All", status="Search actions. Click + to pin; Ctrl+F pins the selected action.";
    private Action pendingInput;
    private int left,top,panelWidth,panelHeight,listTop,listBottom,scroll,selected;

    public CommandPaletteGui(GuiScreen parent){this.parent=parent;}

    @Override public void initGui(){
        panelWidth=Math.min(560,Math.max(280,width-24));
        panelHeight=Math.min(380,Math.max(220,height-24));
        left=(width-panelWidth)/2;top=(height-panelHeight)/2;
        listTop=top+103;listBottom=top+panelHeight-32;
        searchField=new GuiTextField(0,fontRendererObj,left+14,top+43,panelWidth-28,20);
        searchField.setMaxStringLength(100);searchField.setFocused(true);
        actions=createActions();invalidate();Keyboard.enableRepeatEvents(true);clamp();
    }
    @Override public void onGuiClosed(){Keyboard.enableRepeatEvents(false);}

    @Override protected void keyTyped(char ch,int key)throws IOException{
        if(key==Keyboard.KEY_ESCAPE){
            if(pendingInput!=null){pendingInput=null;searchField.setText("");invalidate();status="Input cancelled.";clamp();}
            else mc.displayGuiScreen(parent);
            return;
        }
        if((Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)||Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))&&key==Keyboard.KEY_F){
            toggleSelectedFavorite();return;
        }
        if(pendingInput!=null){
            if(key==Keyboard.KEY_RETURN||key==Keyboard.KEY_NUMPADENTER){submitInput();return;}
            searchField.setFocused(true);searchField.textboxKeyTyped(ch,key);invalidate();return;
        }
        if(key==Keyboard.KEY_UP){move(-1);return;}
        if(key==Keyboard.KEY_DOWN){move(1);return;}
        if(key==Keyboard.KEY_RETURN||key==Keyboard.KEY_NUMPADENTER){executeSelected();return;}
        searchField.setFocused(true);searchField.textboxKeyTyped(ch,key);selected=0;scroll=0;invalidate();clamp();
    }

    @Override protected void mouseClicked(int x,int y,int button)throws IOException{
        super.mouseClicked(x,y,button);searchField.mouseClicked(x,y,button);
        if(button!=0)return;
        if(pendingInput==null&&selectCategory(x,y))return;
        if(pendingInput!=null||y<listTop||y>=listBottom)return;
        int index=scroll+(y-listTop)/ROW_HEIGHT;
        List<Action> filtered=filtered();
        if(index<0||index>=filtered.size())return;
        selected=index;
        if(x>=left+panelWidth-42)toggleFavorite(filtered.get(index));else executeSelected();
    }

    @Override public void handleMouseInput()throws IOException{
        super.handleMouseInput();int wheel=Mouse.getDWheel();
        if(wheel==0||pendingInput!=null)return;
        int max=Math.max(0,filtered().size()-visibleRows());
        scroll=Math.max(0,Math.min(max,scroll+(wheel>0?-1:1)));
        selected=Math.max(scroll,Math.min(selected,scroll+visibleRows()-1));
    }

    @Override public void drawScreen(int mx,int my,float partial){
        drawRect(0,0,width,height,0x99000000);
        drawRect(left,top,left+panelWidth,top+panelHeight,0xF00B0D12);
        drawRect(left,top,left+panelWidth,top+2,ACCENT);
        drawString(fontRendererObj,"COMMAND PALETTE",left+14,top+13,0xFFF2F3F5);
        drawString(fontRendererObj,pendingInput==null?"Search screens, commands, and modules":pendingInput.inputPrompt,left+14,top+28,0xFFB8BDC8);
        searchField.drawTextBox();drawTabs(mx,my);
        drawRect(left+10,listTop-4,left+panelWidth-10,listBottom,0x55212630);
        if(pendingInput!=null){
            drawString(fontRendererObj,"Type a value above and press Enter to run the action.",left+20,listTop+7,0xFFB8BDC8);
        }else{
            List<Action> filtered=filtered();clamp(filtered);
            if(filtered.isEmpty())drawString(fontRendererObj,emptyMessage(),left+20,listTop+7,0xFFB8BDC8);
            for(int row=0;row<visibleRows();row++){
                int index=scroll+row;if(index>=filtered.size())break;
                Action action=filtered.get(index);int y=listTop+row*ROW_HEIGHT;
                if(index==selected)drawRect(left+12,y,left+panelWidth-12,y+ROW_HEIGHT-1,0xAA363B45);
                drawString(fontRendererObj,trim(action.name,panelWidth-56),left+20,y+4,color(action),false);
                drawString(fontRendererObj,trim(action.description,panelWidth-56),left+20,y+17,0xFFB8BDC8,false);
                boolean pinned=CommandPaletteStore.getInstance().isFavorite(action.id);
                drawString(fontRendererObj,pinned?"*":"+",left+panelWidth-31,y+10,pinned?ACCENT:0xFF858B96,false);
            }
        }
        String help=pendingInput==null?"↑/↓ Navigate · Enter/click Run · + Pin · Ctrl+F Pin · Esc Close":"Enter Run · Esc Cancel";
        drawString(fontRendererObj,trim(status+"   "+help,panelWidth-28),left+14,top+panelHeight-18,0xFFB8BDC8);
        super.drawScreen(mx,my,partial);
    }

    private void drawString(net.minecraft.client.gui.FontRenderer font,String text,int x,int y,int color,boolean shadow){
        if(shadow)font.drawStringWithShadow(text,x,y,color);else font.drawString(text,x,y,color);
    }
    private int color(Action a){
        if(a.module==null)return 0xFFF0F1F4;
        if("status".equals(a.kind))return a.module.isUnlocked()&&a.module.isImplemented()?0xFF77DD88:0xFFE38B7C;
        return a.module.isEnabled()?0xFF77DD88:0xFFE6C66B;
    }
    private void drawTabs(int mx,int my){
        int x=left+14;
        for(int i=0;i<CATEGORIES.length;i++){
            String cat=CATEGORIES[i];int row=i<4?0:1;if(i==4)x=left+14;
            int y=top+69+row*16;String label="Favorites".equals(cat)?"Fav":cat;
            int w=fontRendererObj.getStringWidth(label)+12;
            drawRect(x,y,x+w,y+14,category.equals(cat)?0xFF3A3F49:0xAA222630);
            fontRendererObj.drawString(label,x+6,y+3,category.equals(cat)?ACCENT:0xFFD6D9E0);
            x+=w+4;
        }
    }
    private boolean selectCategory(int mx,int my){
        int x=left+14;
        for(int i=0;i<CATEGORIES.length;i++){
            String cat=CATEGORIES[i];int row=i<4?0:1;if(i==4)x=left+14;
            int y=top+69+row*16;String label="Favorites".equals(cat)?"Fav":cat;
            int w=fontRendererObj.getStringWidth(label)+12;
            if(mx>=x&&mx<x+w&&my>=y&&my<y+14){category=cat;selected=0;scroll=0;invalidate();searchField.setFocused(true);clamp();return true;}
            x+=w+4;
        }
        return false;
    }
    private int visibleRows(){return Math.max(1,(listBottom-listTop)/ROW_HEIGHT);}
    private String emptyMessage(){
        if("Favorites".equals(category))return "No pinned actions yet. Click + beside an action to pin it.";
        if("Recent".equals(category))return "No recent actions yet.";
        return "No matching actions.";
    }
    private void move(int direction){
        List<Action> list=filtered();if(list.isEmpty()){selected=0;scroll=0;return;}
        selected=Math.max(0,Math.min(list.size()-1,selected+direction));
        if(selected<scroll)scroll=selected;
        else if(selected>=scroll+visibleRows())scroll=selected-visibleRows()+1;
    }
    private void clamp(){clamp(filtered());}
    private void clamp(List<Action> list){
        if(list.isEmpty()){selected=0;scroll=0;return;}
        selected=Math.max(0,Math.min(selected,list.size()-1));
        scroll=Math.max(0,Math.min(scroll,Math.max(0,list.size()-visibleRows())));
        if(selected<scroll)scroll=selected;
        if(selected>=scroll+visibleRows())scroll=selected-visibleRows()+1;
    }

    private void executeSelected(){
        List<Action> list=filtered();if(selected<0||selected>=list.size())return;
        Action a=list.get(selected);
        if("status".equals(a.kind)){status=a.description;return;}
        if("input".equals(a.kind)){pendingInput=a;searchField.setText("");invalidate();searchField.setFocused(true);status="Waiting for input.";return;}
        if("toggle".equals(a.kind)&&a.module!=null){
            a.module.setEnabled(!a.module.isEnabled());
            status=a.module.getName()+(a.module.isEnabled()?" is on.":" is off.");
            CommandPaletteStore.getInstance().recordUsed(a.id);actions=createActions();invalidate();clamp();return;
        }
        dispatch(a);
    }
    private void submitInput(){
        if(pendingInput==null)return;
        String value=sanitize(searchField.getText());
        if(value.isEmpty()){status="Enter a value first.";return;}
        if(mc==null||mc.thePlayer==null){status="Join a world before using this action.";return;}
        Action a=pendingInput;pendingInput=null;searchField.setText("");invalidate();
        String command=a.commandTemplate.replace("{value}",value);
        dispatchCommand(a.id,a.name,command);
    }
    private void dispatch(Action a){
        if("home".equals(a.id)){CommandPaletteStore.getInstance().recordUsed(a.id);invalidate();mc.displayGuiScreen(new AlphaClientHomeMenu());return;}
        if(a.commandTemplate!=null){
            if(mc==null||mc.thePlayer==null){status="Join a world before using this action.";return;}
            dispatchCommand(a.id,a.name,a.commandTemplate);
        }
    }
    private void dispatchCommand(String id,String name,String command){
        if(mc==null||mc.thePlayer==null){status="Join a world before using this action.";return;}
        CommandPaletteStore.getInstance().recordUsed(id);
        invalidate();
        mc.displayGuiScreen(null);
        mc.thePlayer.sendChatMessage(command);
        status="Running "+name+".";
    }
    private void toggleSelectedFavorite(){List<Action> list=filtered();if(selected>=0&&selected<list.size())toggleFavorite(list.get(selected));}
    private void toggleFavorite(Action a){
        CommandPaletteStore store=CommandPaletteStore.getInstance();
        if(!store.isWritable()){status="Palette history could not be read; pins were left unchanged.";return;}
        boolean pinned=store.toggleFavorite(a.id);
        invalidate();searchField.setFocused(true);
        status=pinned?"Pinned "+a.name+".":"Unpinned "+a.name+".";
        clamp();
    }

    private void invalidate(){filtersDirty=true;}
    private List<Action> filtered(){
        if(pendingInput!=null)return Collections.emptyList();
        if(!filtersDirty)return filteredCache;
        filtersDirty=false;
        String query=searchField==null?"":searchField.getText().trim().toLowerCase(Locale.ROOT);
        Set<String> favorites=CommandPaletteStore.getInstance().getFavoriteIds();
        List<String> recent=CommandPaletteStore.getInstance().getRecentIds();
        List<Action> out=new ArrayList<Action>();
        for(Action a:actions){
            if(!"All".equals(category)&&!"Recent".equals(category)&&!"Favorites".equals(category)&&!category.equals(a.category))continue;
            if("Recent".equals(category)&&!recent.contains(a.id))continue;
            if("Favorites".equals(category)&&!favorites.contains(a.id))continue;
            a.score=query.isEmpty()?0:score(a,query);
            if(a.score<Integer.MAX_VALUE)out.add(a);
        }
        final boolean hasQuery=!query.isEmpty(), favOnly="Favorites".equals(category), recentOnly="Recent".equals(category);
        Collections.sort(out,new Comparator<Action>(){
            public int compare(Action a,Action b){
                if(hasQuery&&a.score!=b.score)return Integer.compare(a.score,b.score);
                if(recentOnly){int r=CommandPaletteStore.getInstance().recentIndex(a.id)-CommandPaletteStore.getInstance().recentIndex(b.id);if(r!=0)return r;}
                int af=CommandPaletteStore.getInstance().isFavorite(a.id)?1:0;
                int bf=CommandPaletteStore.getInstance().isFavorite(b.id)?1:0;
                if(af!=bf)return bf-af;
                if(favOnly){int r=CommandPaletteStore.getInstance().favoriteIndex(a.id)-CommandPaletteStore.getInstance().favoriteIndex(b.id);if(r!=0)return r;}
                int ar=CommandPaletteStore.getInstance().recentIndex(a.id),br=CommandPaletteStore.getInstance().recentIndex(b.id);
                if(ar!=br)return Integer.compare(ar,br);
                return a.name.compareToIgnoreCase(b.name);
            }
        });
        filteredCache=Collections.unmodifiableList(out);
        return filteredCache;
    }
    private int score(Action a,String q){
        String name=a.name.toLowerCase(Locale.ROOT);
        String all=(name+" "+a.description+" "+a.category+" "+(a.commandTemplate==null?"":a.commandTemplate)+" "+(a.module==null?"":a.module.getId())).toLowerCase(Locale.ROOT);
        if(name.equals(q))return 0;if(name.startsWith(q))return 10+name.length()-q.length();
        int i=name.indexOf(q);if(i>=0)return 30+i;i=all.indexOf(q);if(i>=0)return 60+i;
        int fuzzy=subsequence(q,name);if(fuzzy<Integer.MAX_VALUE)return 100+fuzzy;
        fuzzy=subsequence(q,all);return fuzzy==Integer.MAX_VALUE?Integer.MAX_VALUE:500+fuzzy;
    }
    private int subsequence(String q,String text){
        int previous=-1,gaps=0;
        for(int i=0;i<q.length();i++){int next=text.indexOf(q.charAt(i),previous+1);if(next<0)return Integer.MAX_VALUE;if(previous>=0)gaps+=next-previous-1;previous=next;}
        return gaps;
    }

    private List<Action> createActions(){
        List<Action> out=new ArrayList<Action>();
        out.add(Action.screen("home","Open AlphaClient home menu","Open the AlphaClient overlay and HUD controls.",null));
        out.add(Action.screen("general","Browse General modules","Open the public module browser.","/general"));
        out.add(Action.screen("quality","Open Quality settings","Open Quality module settings.","/quality"));
        out.add(Action.screen("hud-editor","Open HUD editor","Move and configure supported HUD elements.","/hudeditor"));
        out.add(Action.screen("help","Open AlphaClient help","Browse commands and help entries.","/alphahelp open"));
        out.add(Action.screen("staff-operations","Open Staff Operations","Opens only when your account has access.","/staffoperations"));
        out.add(Action.screen("waypoints","Open Waypoints","Manage saved waypoint markers.","/waypoint"));
        out.add(Action.screen("waypoint-visuals","Open Waypoint display settings","Change markers, beams, labels, and sorting.","/waypoint visuals"));

        out.add(Action.input("quick-waypoint-add","Create waypoint","Save your current position with a name.","Waypoints","/waypoint add \"{value}\"","Waypoint name"));
        out.add(Action.input("quick-waypoint-select","Select waypoint","Set a waypoint as your navigation target.","Waypoints","/waypoint select \"{value}\"","Waypoint name"));
        out.add(Action.input("quick-waypoint-profile","Switch waypoint profile","Switch to an existing or new local profile.","Waypoints","/waypoint profile \"{value}\"","Profile name"));
        out.add(Action.input("quick-waypoint-favorite","Toggle waypoint favorite","Pin or unpin a saved waypoint.","Waypoints","/waypoint favorite \"{value}\"","Waypoint name"));

        out.add(Action.command("waypoint-list","List waypoints","Show active-profile waypoints in chat.","Commands","/waypoint list"));
        out.add(Action.command("waypoint-history","Show coordinate history","List recently sampled positions in chat.","Commands","/waypoint history"));
        out.add(Action.command("waypoint-export","Export waypoints","Write the active profile to the local share file.","Commands","/waypoint export"));
        out.add(Action.command("module-list","List module status","Print registered module states in chat.","Commands","/alphaclient:modules"));

        Set<String> seen=new HashSet<String>();
        for(AlphaModule m:AlphaModuleRegistry.getAll()){
            if(m==null||!seen.add(m.getId()))continue;
            String state=m.getStatusLabel();
            String detail="Status: "+state+" · "+m.getCategory().getTitle()+". "+m.getDescription();
            if(!m.isImplemented()||!m.isUnlocked()){
                out.add(Action.status("module-status:"+m.getId(),m.getName(),detail,m));continue;
            }
            out.add(Action.toggle("module-toggle:"+m.getId(),"Toggle "+m.getName(),"Status: "+state+". "+m.getDescription(),m));
            out.add(Action.moduleSettings("module-settings:"+m.getId(),"Configure "+m.getName(),"Open settings for "+m.getName(),"/alphaclient:settings "+m.getId(),m));
        }
        Collections.sort(out,new Comparator<Action>(){public int compare(Action a,Action b){return a.name.compareToIgnoreCase(b.name);}});
        return Collections.unmodifiableList(out);
    }

    private String sanitize(String value){return value==null?"":value.replace("\"","").replace("\r"," ").replace("\n"," ").trim();}
    private String trim(String value,int max){if(fontRendererObj.getStringWidth(value)<=max)return value;return fontRendererObj.trimStringToWidth(value,Math.max(0,max-8))+"...";}
    @Override public boolean doesGuiPauseGame(){return false;}

    private static final class Action{
        final String id,name,description,category,kind,commandTemplate,inputPrompt;
        final AlphaModule module;int score;
        private Action(String id,String name,String description,String category,String kind,String command,String prompt,AlphaModule module){
            this.id=id;this.name=name;this.description=description;this.category=category;this.kind=kind;this.commandTemplate=command;this.inputPrompt=prompt;this.module=module;
        }
        static Action screen(String id,String name,String desc,String command){return screen(id,name,desc,command,null);}
        static Action screen(String id,String name,String desc,String command,AlphaModule module){return new Action(id,name,desc,"Screens","screen",command,null,module);}
        static Action command(String id,String name,String desc,String category,String command){return new Action(id,name,desc,category,"command",command,null,null);}
        static Action input(String id,String name,String desc,String category,String command,String prompt){return new Action(id,name,desc,category,"input",command,prompt,null);}
        static Action moduleSettings(String id,String name,String desc,String command,AlphaModule module){return new Action(id,name,desc,"Modules","screen",command,null,module);}
        static Action toggle(String id,String name,String desc,AlphaModule module){return new Action(id,name,desc,"Modules","toggle",null,null,module);}
        static Action status(String id,String name,String desc,AlphaModule module){return new Action(id,name,desc,"Modules","status",null,null,module);}
    }
}
