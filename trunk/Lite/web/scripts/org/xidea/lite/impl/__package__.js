this.addScript('template.js',["TemplateImpl","PLUGIN_DEFINE",'VAR_TYPE','XA_TYPE','ELSE_TYPE','PLUGIN_TYPE','CAPTRUE_TYPE','IF_TYPE','EL_TYPE','BREAK_TYPE','XT_TYPE','FOR_TYPE']
                ,0
                ,['org.xidea.lite.impl.js:JSTranslator','org.xidea.lite.parse:ParseContext','org.xidea.jsi:$log','org.xidea.el:evaluate']);

this.addScript('translate-context.js','TranslateContext'
                ,0
                ,[
                	"org.xidea.el:*",
                	'org.xidea.jsi:$log']);
                
this.addDependence("*",'*',true);