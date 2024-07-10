import { TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness, RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { SessionPackageDetailComponent } from './session-package-detail.component';

describe('SessionPackage Management Detail Component', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SessionPackageDetailComponent, RouterTestingModule.withRoutes([], { bindToComponentInputs: true })],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              component: SessionPackageDetailComponent,
              resolve: { sessionPackage: () => of({ id: 'ABC' }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(SessionPackageDetailComponent, '')
      .compileComponents();
  });

  describe('OnInit', () => {
    it('Should load sessionPackage on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', SessionPackageDetailComponent);

      // THEN
      expect(instance.sessionPackage).toEqual(expect.objectContaining({ id: 'ABC' }));
    });
  });
});
