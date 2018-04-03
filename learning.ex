% vaimenna turhat hälytykset 
warning("off");

%%% FUNKTIOIDEN MÄÄRITTELYT %%%

% kustannusfunktio virheiden kumulatiiviseen arviointiin
function O = cost(Theta1, Theta2, X, Y),
	O = 0;
	m = length(Y);
	for i = 1:m,
		for k = 1:6,
			[h, x] = forward_propagate(Theta1, Theta2, X(i, :));
			a = Y(i, k)*log(x(k));
			b = (1 - Y(i,k)) * log(1 - x(k));
			O += a + b;
		end;
	end;
	O = -O./m;
end;

% lopullinen luokan ennustaminen
function P = predict(X),
	P = (X>0.5);
end;

% logistinen funktio painotettujen arvojen käsittelemiseen
function O = g(Z),
	O = 1 ./ (1 + exp(-Z));
end;

% forward propagation -funktio syötearvojen kuljettamiseksi verkon läpi
function [H, O] = forward_propagate(Theta1, Theta2, X),
	Xb = biased(X)';
	H = (g(Theta1 * Xb))'; % hidden layer
	Hb = biased(H)';
	O = g(Theta2 * (biased(g(Theta1 * biased(X)')')'));
	O = (g(Theta2 * Hb))'; % output layer
end;

% back propagation -funktio kerros- ja solukohtaisten virheiden arvioimiseksi
function [delta2, delta3] = back_propagate(Theta1, Theta2, H, O, Y),
	delta3 = O .- Y;
	delta2 = ((Theta2(:,2:end)' * delta3')') .* (H .* (1 .- H)); 
end;
	
% bias-yksikön lisäys
function B = biased(X),
	B = [1 X];
end;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% luetaan ja sekoitetaan data-CSV
CSVT = csvread('dataset.csv');
CSV = [];
l = length(CSVT);
ll = l;
for i = 1:ll,
	CSV = [CSV; CSVT(randi(l),:)];
	CSVT = [ CSVT(1:(l-1),:); CSVT((l+1):end,:) ];
	l -= 1;
end;

% ositteluparametri
vpos = 0.8 * ll;

% ositellaan CSV-tiedosto validointiosaan
CSVV = CSV((vpos+1):end,:);

% ... ja opetusosaan
CSV = CSV(1:vpos,:);

% opetuskuvien piirteet
X_raw = CSV(:,2:end);

% keskiarvot ja keskihajonnat
X_means = mean(X_raw);
X_stds = std(X_raw);

% näytearvojen normalisointi
X = (X_raw .- X_means) ./ X_stds;

% näytteiden määrä
m = length(X);

% näytteiden luokat
Y_labels = CSV(:,1);

% luodaan luokista vektoroidut versiot 
Y = zeros(m, 6);
for i = 1:m,
	Y(i, Y_labels(i)) = 1;
end;

% satunnaisluvuilla alustetut painotusmatriisit
Theta1 = rand(6, 7);
Theta2 = rand(6, 7);

% piilotetun kerroksen aktivointiarvot
H = zeros(size(X,1), 6);

% lopputuloskerroksen aktivointi
O = zeros(size(X,1), 6);

% oppimisnopeus
alpha = 0.2;

% oppimiskierrosten määrä
iters = 5000;

printf("Learning for %d iterations.", iters);
for k = 1:iters,
	% kumulatiiviset deltat (kumulatiiviset virheet)
	Delta1 = zeros(6,7);
	Delta2 = zeros(6,7);

	% lasketaan Deltat
	for i = 1:m,
		[H, O] = forward_propagate(Theta1, Theta2, X(i,:));
		[delta2, delta3] = back_propagate(Theta1, Theta2, H, O, Y(i,:));
		Delta1 += delta2' * biased(X(i,:));
		Delta2 += delta3' * biased(H); 
	end;
	
	% reguloimattomat gradientit 
	grad_unreg1 = Delta1 ./ m;
	grad_unreg2 = Delta2 ./ m;
	

	% gradient descent
	for i = 1:6,
		for j = 1:7,
			Theta1(i, j) -= alpha * grad_unreg1(i, j);
			Theta2(i, j) -= alpha * grad_unreg2(i, j);
		end;
	end;

	if !mod(k, 50),
		printf("\nIteration:\t%d\n", k);
		printf("cost:\t\t%f\n", cost(Theta1, Theta2, X, Y));	
	end;
end;
disp("\nDone.\n");
disp("Predict training samples.\n");
pause(5);

for i = 1:m,
	[H, O] = forward_propagate(Theta1, Theta2, X(i, :));
	P = predict(O);
	predicted = find(P);
	if length(predicted) == 0 || length(predicted) > 1,
		predicted = 0;
	end;
	actual = find(Y(i,:));
	%disp(O);
	printf("Learning sample %d:\t actual %d, detected %d\n", i, actual, predicted);
end;

disp("\n\nPredicting validation samples.\n");
pause(5);

% validointinäytteiden luku ja normalisointi
X_raw = CSVV(:,2:end);
X = (X_raw .- X_means) ./ X_stds;
m = length(X);
Y_labels = CSVV(:,1);

Y = zeros(m, 6);
for i = 1:m,
	Y(i, Y_labels(i)) = 1;
end;

for i = 1:m,
	[H, O] = forward_propagate(Theta1, Theta2, X(i, :));
	P = predict(O);
	predicted = find(P);
	if length(predicted) == 0 || length(predicted) > 1,
		predicted = 0;
	end;
	actual = find(Y(i,:));
	%disp(O);
	printf("Validation sample %d\t actual %d, detected %d\n", i, actual, predicted);
end;

disp("Writing learned data on disk.\n");
csvwrite('theta1.csv', Theta1);
csvwrite('theta2.csv', Theta2);
csvwrite('mean.csv', X_means);
csvwrite('std.csv', X_stds);
disp("Done.\n");
